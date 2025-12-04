
import json

weapons = [
    "AWP", "AK-47", "M4A4", "Desert Eagle", 
    "Karambit", "Butterfly Knife", "USP-S", "Glock-18"
]

skins = [
    "Dragon Lore", "Asiimov", "Redline", "Fade", "Hyper Beast", 
    "Neo-Noir", "Vulcan", "Case Hardened", "Doppler", "Howl"
]

# Existing images from itemImages.js
images = {
  "AK-47 | Case Hardened": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyLwlcK3wiNK0P2nZKFpH_yaCW-Ej7sk5bE8Sn-2lEpz4zndzoyvdHuUPwFzWZYiE7EK4Bi4k9TlY-y24FbAy9USGSiZd5Q",
  "AK-47 | Asiimov": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyLwlcK3wiFO0POlPPNSIeOaB2qf19F6ueZhW2e2wEt-t2jcytf6dymSO1JxA5oiRecLsRa5kIfkYr-241aLgotHz3-rkGoXuUp8oX57",
  "AK-47 | Redline": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyLwlcK3wiFO0POlPPNSI_-RHGavzedxuPUnFniykEtzsWWBzoyuIiifaAchDZUjTOZe4RC_w4buM-6z7wzbgokUyzK-0H08hRGDMA",
  "AK-47 | Vulcan": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyLwlcK3wiFO0POlPPNSMuWRDGKC_uJ_t-l9AXCxxEh14zjTztivci2ePQZ2W8NzTecD4BKwloLiYeqxtAOIj9gUyyngznQeF7I6QE8",
  "AWP | Fade": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyLwiYbf_CNk7uW-V6JsJPWsAm6Xyfo45-c5GXDnwB534DuEwtuoIHOfaAYiAsYjF-QItUaxmoC0MO_h5ALcjJUFk3sEzfdk4w",
  "AWP | Asiimov": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyLwiYbf_jdk7uW-V6V-Kf2cGFidxOp_pewnF3nhxEt0sGnSzN76dH3GOg9xC8FyEORftRe-x9PuYurq71bW3d8UnjK-0H0YSTpMGQ",
  "AWP | Redline": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyLwiYbf_jdk7uW-V6diIuKSMWuZxuZi_rUxHS3lzUwm5DjWy976dSiRagd1WJB1RLQP4RK-mtazM-3itQeL2INbjXKpw2eVIZ0",
  "AWP | Hyper Beast": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyLwiYbf_jdk7uW-V6x0MPWBMWWVwP1ij-1gSCGn20pxtm_WzNuoeHKeaFAnCZUiTe5bt0HqxofmZOrm5Q2IjoMQzS_5iShXrnE8NzWs__c",
  "AWP | Neo-Noir": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyLwiYbf_jdk7uW-V6poL_6cB3WvzedxuPUnHirrxR4l423SyI39I3KXPwdxWZclQeNZ5EXskYfnNeyw71OMi9lNzDK-0H3r66pOTw",
  "AWP | Dragon Lore": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyLwiYbf_jdk4veqYaF7IfysCnWRxuF4j-B-Xxa_nBovp3Pdwtj9cC_GaAd0DZdwQu9fuhS4kNy0NePntVTbjYpCyyT_3CgY5i9j_a9cBkcCWUKV",
  "Glock-18 | Fade": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyL2kpnj9h1a7s2oaaBoH_yaCW-Ej-8u5bZvHnq1w0Vz62TUzNj4eCiVblMmXMAkROJeskLpkdXjMrzksVTAy9US8PY25So",
  "Glock-18 | Neo-Noir": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyL2kpnj9h1Y-s2pZKtuK8-dAW6C_uJ_t-l9AXznwh9zsjjSn9j9dH-eb1V0CsF3QrNZ4xW8ltPlM-7h4QbYit5NzyzgznQecekkTuo",
  "Butterfly Knife | Fade": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyL6kJ_m-B1Z-ua6bbZrLOmsD2avx-9ytd5lRi67gVNwsDvSwtqqc3iXZg4kCZYjReYLtRbum9XgYuvm5wbWjtgUzCn3iSsf8G81tFEeH9rw",
  "Butterfly Knife | Doppler": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyL6kJ_m-B1Z-ua6bbZrLOmsD2qvw-J3s-p5SiihmSIqsi-HlorwOy7DAVRPVssnHaMUuhe9xIHlMuvqtgPf2IoTyC383Sod7CY-sr4DVfZ2qKPU3g-TNuE-545DeqjFvb87vg",
  "Butterfly Knife | Case Hardened": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyL6kJ_m-B1Z-ua6bbZrLOmsD3avzud6teVWQyC0nQlp5z6AyN_7I3mfOFQnApUlFrMN5BbpwdbhP-vgs1Pd3dpBmXr9jnwf6DErvbim1G57Bg",
  "Karambit | Fade": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyL6kJ_m-B1Q7uCvZaZkNM-SD1iWwOpzj-1gSCGn20tztm_UyIn_JHKUbgYlWMcmQ-ZcskSwldS0MOnntAfd3YlMzH35jntXrnE8SOGRGG8",
  "Karambit | Doppler": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyL6kJ_m-B1Q7uCvZaZkNM-SA1iSze91u_FsTju_qhAmoT-Jn4bjJC_4Ml93UtZuRLQPsBawkNfiMbnl5AKMiopCnin7iCJBv31j4rkBBKEg-6zUjV3GY6p9v8dpLWT3Fg",
  "Karambit | Case Hardened": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyL6kJ_m-B1Q7uCvZaZkNM-SH1ifyOJztN5lRi67gVNz5DvUmdj4eXuWOFAhAsF4RLFc5BC4xtbuY7yx7wDbgo9CzSj2h3xK8G81tB_XeHWq",
  "M4A4 | Asiimov": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyL8ypexwiFO0P_6V6V-Kf2cGFidxOp_pewnTii3w0x_tmTRnt2qdHyWaFAjA5UlQOYI5BO5k9bhZunm41OI34NDnjK-0H3pAWw_Rw",
  "M4A4 | Howl": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyL8ypexwiFO0P_6afVSKP-EAm6extF6ueZhW2exwkl2tmTXwt39eCiUPQR2DMN4TOVetUK8xoLgM-K341eM2otDnC6okGoXufBz_TAB",
  "M4A4 | Neo-Noir": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyL8ypexwiFO0P_6afBSLvWcMWmfyPxJvOhuRz39wE1142vSztmvInvBOgV0W5R1FLYNuxW4wIbgNrmx4g2Kj4tMmCX93zQJsHgJr0dqFw",
  "USP-S | Neo-Noir": "https://community.akamai.steamstatic.com/economy/image/i0CoZ81Ui0m-9KwlBY1L_18myuGuq1wfhWSaZgMttyVfPaERSR0Wqmu7LAocGIGz3UqlXOLrxM-vMGmW8VNxu5Dx60noTyLkjYbf7itX6vytbbZSI-WsG3SA0tF4v-h7cCW6khUz_WXdmd-vI3uRPwEkApR4QuBcu0Xrk4biYr_mtQXdidlCz3r63Ska7Hx1o7FVWuokIcU"
}

# Fallback mappings
fallbacks = {
    "AWP": {
        "default": "AWP | Asiimov",
        "Dragon Lore": "AWP | Dragon Lore",
        "Asiimov": "AWP | Asiimov",
        "Redline": "AWP | Redline",
        "Fade": "AWP | Fade",
        "Hyper Beast": "AWP | Hyper Beast",
        "Neo-Noir": "AWP | Neo-Noir",
        "Vulcan": "AWP | Asiimov",
        "Case Hardened": "AWP | Dragon Lore",
        "Doppler": "AWP | Fade",
        "Howl": "AWP | Redline"
    },
    "AK-47": {
        "default": "AK-47 | Vulcan",
        "Dragon Lore": "AK-47 | Case Hardened",
        "Asiimov": "AK-47 | Asiimov",
        "Redline": "AK-47 | Redline",
        "Fade": "AK-47 | Case Hardened",
        "Hyper Beast": "AK-47 | Vulcan",
        "Neo-Noir": "AK-47 | Vulcan",
        "Vulcan": "AK-47 | Vulcan",
        "Case Hardened": "AK-47 | Case Hardened",
        "Doppler": "AK-47 | Vulcan",
        "Howl": "AK-47 | Redline"
    },
    "M4A4": {
        "default": "M4A4 | Howl",
        "Dragon Lore": "M4A4 | Howl",
        "Asiimov": "M4A4 | Asiimov",
        "Redline": "M4A4 | Howl",
        "Fade": "M4A4 | Neo-Noir",
        "Hyper Beast": "M4A4 | Neo-Noir",
        "Neo-Noir": "M4A4 | Neo-Noir",
        "Vulcan": "M4A4 | Asiimov",
        "Case Hardened": "M4A4 | Asiimov",
        "Doppler": "M4A4 | Neo-Noir",
        "Howl": "M4A4 | Howl"
    },
    "Desert Eagle": {
        "default": "https://placehold.co/300x200/333/fff?text=Desert+Eagle"
    },
    "Karambit": {
        "default": "Karambit | Fade",
        "Dragon Lore": "Karambit | Case Hardened",
        "Asiimov": "Karambit | Case Hardened",
        "Redline": "Karambit | Fade",
        "Fade": "Karambit | Fade",
        "Hyper Beast": "Karambit | Doppler",
        "Neo-Noir": "Karambit | Doppler",
        "Vulcan": "Karambit | Doppler",
        "Case Hardened": "Karambit | Case Hardened",
        "Doppler": "Karambit | Doppler",
        "Howl": "Karambit | Fade"
    },
    "Butterfly Knife": {
        "default": "Butterfly Knife | Fade",
        "Dragon Lore": "Butterfly Knife | Case Hardened",
        "Asiimov": "Butterfly Knife | Case Hardened",
        "Redline": "Butterfly Knife | Fade",
        "Fade": "Butterfly Knife | Fade",
        "Hyper Beast": "Butterfly Knife | Doppler",
        "Neo-Noir": "Butterfly Knife | Doppler",
        "Vulcan": "Butterfly Knife | Doppler",
        "Case Hardened": "Butterfly Knife | Case Hardened",
        "Doppler": "Butterfly Knife | Doppler",
        "Howl": "Butterfly Knife | Fade"
    },
    "USP-S": {
        "default": "USP-S | Neo-Noir"
    },
    "Glock-18": {
        "default": "Glock-18 | Fade",
        "Dragon Lore": "Glock-18 | Fade",
        "Asiimov": "Glock-18 | Neo-Noir",
        "Redline": "Glock-18 | Fade",
        "Fade": "Glock-18 | Fade",
        "Hyper Beast": "Glock-18 | Neo-Noir",
        "Neo-Noir": "Glock-18 | Neo-Noir",
        "Vulcan": "Glock-18 | Neo-Noir",
        "Case Hardened": "Glock-18 | Fade",
        "Doppler": "Glock-18 | Fade",
        "Howl": "Glock-18 | Fade"
    }
}

result = {}

for weapon in weapons:
    for skin in skins:
        key = f"{weapon} | {skin}"
        if key in images:
            result[key] = images[key]
        else:
            # Find fallback
            fallback_map = fallbacks.get(weapon, {})
            fallback_key = fallback_map.get(skin)
            
            if fallback_key and fallback_key in images:
                result[key] = images[fallback_key]
            elif fallback_key and fallback_key.startswith("http"):
                 result[key] = fallback_key
            elif "default" in fallback_map:
                default_val = fallback_map["default"]
                if default_val in images:
                    result[key] = images[default_val]
                else:
                    result[key] = default_val
            else:
                # Absolute fallback
                result[key] = f"https://placehold.co/300x200/333/fff?text={weapon.replace(' ', '+')}"

with open("skin_mapping.json", "w") as f:
    json.dump(result, f, indent=2)
