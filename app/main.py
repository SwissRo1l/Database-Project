from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional, List
import sqlite3

from .db import init_db, transaction, get_connection

app = FastAPI(title="Game Market API", version="0.1.0")

# Allow frontend dev server to call APIs
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
def root():
    return {
        "message": "Game Market API running",
        "try": ["/health", "/docs"],
        "note": "This root path has no page; use /docs for Swagger UI."
    }


class PlayerCreate(BaseModel):
    username: str = Field(min_length=3, max_length=32)


class DepositRequest(BaseModel):
    username: str
    currency: str = Field(default="GOLD")
    amount: int = Field(gt=0)


class MintItemRequest(BaseModel):
    username: str
    item_type: str
    metadata: Optional[str] = None


class ListingCreate(BaseModel):
    username: str
    item_id: int
    currency: str = Field(default="GOLD")
    price: int = Field(gt=0)


class PurchaseRequest(BaseModel):
    username: str
    listing_id: int


@app.on_event("startup")
def on_startup():
    # Initialize schema if not exists
    init_db(seed=True)


@app.post("/players")
def create_player(body: PlayerCreate):
    with transaction() as conn:
        cur = conn.execute("INSERT INTO players(username) VALUES (?)", (body.username,))
        player_id = cur.lastrowid
        # Ensure default wallet
        conn.execute(
            "INSERT OR IGNORE INTO currencies(code, name, decimals) VALUES('GOLD','Gold Coin',2)"
        )
        conn.execute(
            "INSERT OR IGNORE INTO wallets(player_id, currency_code, balance) VALUES(?, 'GOLD', 0)",
            (player_id,),
        )
        return {"id": player_id, "username": body.username}


@app.post("/wallets/deposit")
def deposit(body: DepositRequest):
    with transaction() as conn:
        row = conn.execute("SELECT id FROM players WHERE username=?", (body.username,)).fetchone()
        if not row:
            raise HTTPException(404, "player not found")
        player_id = row[0]
        # ensure currency exists
        c = conn.execute("SELECT 1 FROM currencies WHERE code=?", (body.currency,)).fetchone()
        if not c:
            raise HTTPException(400, "currency not supported")
        # upsert wallet
        w = conn.execute(
            "SELECT id FROM wallets WHERE player_id=? AND currency_code=?",
            (player_id, body.currency),
        ).fetchone()
        if w:
            conn.execute("UPDATE wallets SET balance = balance + ? WHERE id=?", (body.amount, w[0]))
            wallet_id = w[0]
        else:
            cur = conn.execute(
                "INSERT INTO wallets(player_id, currency_code, balance) VALUES(?,?,?)",
                (player_id, body.currency, body.amount),
            )
            wallet_id = cur.lastrowid
        conn.execute(
            "INSERT INTO ledger_entries(wallet_id, amount, reason) VALUES(?,?,?)",
            (wallet_id, body.amount, "deposit"),
        )
        bal = conn.execute("SELECT balance FROM wallets WHERE id=?", (wallet_id,)).fetchone()[0]
        return {"username": body.username, "currency": body.currency, "balance": bal}


@app.post("/items/mint")
def mint_item(body: MintItemRequest):
    with transaction() as conn:
        p = conn.execute("SELECT id FROM players WHERE username=?", (body.username,)).fetchone()
        if not p:
            raise HTTPException(404, "player not found")
        type_row = conn.execute("SELECT id FROM item_types WHERE name=?", (body.item_type,)).fetchone()
        if not type_row:
            raise HTTPException(400, "item type not found")
        cur = conn.execute(
            "INSERT INTO items(type_id, owner_id, status, metadata) VALUES(?, ?, 'in_inventory', ?)",
            (type_row[0], p[0], body.metadata),
        )
        return {"item_id": cur.lastrowid}


@app.post("/listings")
def create_listing(body: ListingCreate):
    with transaction() as conn:
        p = conn.execute("SELECT id FROM players WHERE username=?", (body.username,)).fetchone()
        if not p:
            raise HTTPException(404, "player not found")
        item = conn.execute(
            "SELECT id, owner_id, status FROM items WHERE id=?",
            (body.item_id,),
        ).fetchone()
        if not item:
            raise HTTPException(404, "item not found")
        if item[1] != p[0] or item[2] != 'in_inventory':
            raise HTTPException(400, "item not eligible for listing")
        # lock item
        conn.execute(
            "UPDATE items SET status='listed' WHERE id=? AND status='in_inventory'",
            (body.item_id,),
        )
        cur = conn.execute(
            "INSERT INTO listings(item_id, seller_id, currency_code, price, status) VALUES(?,?,?,?, 'active')",
            (body.item_id, p[0], body.currency, body.price),
        )
        return {"listing_id": cur.lastrowid}


@app.get("/listings")
def list_active_listings():
    conn = get_connection()
    try:
        rows = conn.execute(
            """
            SELECT l.id as listing_id, l.price, l.currency_code, i.id as item_id, t.name as item_type, u.username as seller
            FROM listings l
            JOIN items i ON i.id = l.item_id
            JOIN item_types t ON t.id = i.type_id
            JOIN players u ON u.id = l.seller_id
            WHERE l.status = 'active'
            ORDER BY l.created_at DESC
            """
        ).fetchall()
        return [dict(r) for r in rows]
    finally:
        conn.close()


@app.post("/orders/purchase")
def purchase(body: PurchaseRequest):
    with transaction() as conn:
        buyer = conn.execute("SELECT id FROM players WHERE username=?", (body.username,)).fetchone()
        if not buyer:
            raise HTTPException(404, "buyer not found")
        listing = conn.execute(
            "SELECT id, item_id, seller_id, currency_code, price, status FROM listings WHERE id=?",
            (body.listing_id,),
        ).fetchone()
        if not listing or listing[5] != 'active':
            raise HTTPException(400, "listing not available")
        if listing[2] == buyer[0]:
            raise HTTPException(400, "cannot buy your own listing")

        currency = listing[3]
        price = listing[4]

        # fee
        fee_bps_row = conn.execute("SELECT value FROM settings WHERE key='market_fee_bps'").fetchone()
        fee_bps = int(fee_bps_row[0]) if fee_bps_row else 0
        fee = (price * fee_bps) // 10000
        seller_receipt = price - fee

        # check buyer balance
        w_buyer = conn.execute(
            "SELECT id, balance FROM wallets WHERE player_id=? AND currency_code=?",
            (buyer[0], currency),
        ).fetchone()
        if not w_buyer or w_buyer[1] < price:
            raise HTTPException(400, "insufficient balance")

        # ensure seller wallet exists
        w_seller = conn.execute(
            "SELECT id FROM wallets WHERE player_id=? AND currency_code=?",
            (listing[2], currency),
        ).fetchone()
        if not w_seller:
            cur = conn.execute(
                "INSERT INTO wallets(player_id, currency_code, balance) VALUES(?,?,0)",
                (listing[2], currency),
            )
            w_seller_id = cur.lastrowid
        else:
            w_seller_id = w_seller[0]

        # perform transfer and close listing atomically
        conn.execute("UPDATE wallets SET balance = balance - ? WHERE id=?", (price, w_buyer[0]))
        conn.execute("INSERT INTO ledger_entries(wallet_id, amount, reason, ref_table, ref_id) VALUES(?,?,?,?,?)",
                     (w_buyer[0], -price, 'purchase', 'listings', listing[0]))

        conn.execute("UPDATE wallets SET balance = balance + ? WHERE id=?", (seller_receipt, w_seller_id))
        conn.execute("INSERT INTO ledger_entries(wallet_id, amount, reason, ref_table, ref_id) VALUES(?,?,?,?,?)",
                     (w_seller_id, seller_receipt, 'sale_proceeds', 'listings', listing[0]))

        # market fee sink wallet: player_id NULL -> create singleton wallet row? For MVP omit custody
        # Alternatively, record fee in ledger without wallet balance effect
        # Here we just record a ledger entry with wallet_id = seller wallet for trace.

        # transfer item ownership
        conn.execute("UPDATE items SET owner_id=?, status='sold' WHERE id=?", (buyer[0], listing[1]))
        conn.execute("UPDATE listings SET status='sold' WHERE id=?", (listing[0],))

        conn.execute(
            "INSERT INTO orders(listing_id, buyer_id, currency_code, price_paid, fee_amount, seller_receipt) VALUES(?,?,?,?,?,?)",
            (listing[0], buyer[0], currency, price, fee, seller_receipt),
        )

        return {"status": "ok", "listing_id": listing[0], "item_id": listing[1], "price": price}


@app.get("/health")
def health():
    try:
        conn = get_connection()
        conn.execute("SELECT 1")
        return {"status": "up"}
    except sqlite3.Error as e:
        raise HTTPException(500, str(e))
    finally:
        try:
            conn.close()
        except Exception:
            pass
