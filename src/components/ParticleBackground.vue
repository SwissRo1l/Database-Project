<template>
  <canvas ref="canvas" class="particle-bg"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvas = ref(null)
let ctx = null
let animationFrameId = null
let particles = []
let mouse = { x: null, y: null, radius: 150 }

const props = defineProps({
  particleCount: {
    type: Number,
    default: 80
  },
  connectionDistance: {
    type: Number,
    default: 100
  },
  mouseDistance: {
    type: Number,
    default: 150
  }
})

class Particle {
  constructor(x, y, directionX, directionY, size, color) {
    this.x = x
    this.y = y
    this.directionX = directionX
    this.directionY = directionY
    this.size = size
    this.color = color
  }

  draw() {
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2, false)
    ctx.fillStyle = this.color
    ctx.fill()
  }

  update() {
    if (this.x > canvas.value.width || this.x < 0) {
      this.directionX = -this.directionX
    }
    if (this.y > canvas.value.height || this.y < 0) {
      this.directionY = -this.directionY
    }

    // Mouse interaction
    let dx = mouse.x - this.x
    let dy = mouse.y - this.y
    let distance = Math.sqrt(dx * dx + dy * dy)

    if (distance < mouse.radius + this.size) {
      if (mouse.x < this.x && this.x < canvas.value.width - this.size * 10) {
        this.x += 2 // Push away slightly or move with mouse
      }
      if (mouse.x > this.x && this.x > this.size * 10) {
        this.x -= 2
      }
      if (mouse.y < this.y && this.y < canvas.value.height - this.size * 10) {
        this.y += 2
      }
      if (mouse.y > this.y && this.y > this.size * 10) {
        this.y -= 2
      }
    }

    this.x += this.directionX
    this.y += this.directionY
    this.draw()
  }
}

const init = () => {
  particles = []
  let numberOfParticles = (canvas.value.width * canvas.value.height) / 9000
  // Cap particles for performance
  if (numberOfParticles > 150) numberOfParticles = 150 
  
  for (let i = 0; i < numberOfParticles; i++) {
    let size = (Math.random() * 2) + 1
    let x = (Math.random() * ((canvas.value.width - size * 2) - (size * 2)) + size * 2)
    let y = (Math.random() * ((canvas.value.height - size * 2) - (size * 2)) + size * 2)
    let directionX = (Math.random() * 1) - 0.5
    let directionY = (Math.random() * 1) - 0.5
    let color = 'rgba(255, 255, 255, 0.4)'

    particles.push(new Particle(x, y, directionX, directionY, size, color))
  }
}

const connect = () => {
  let opacityValue = 1
  for (let a = 0; a < particles.length; a++) {
    for (let b = a; b < particles.length; b++) {
      let distance = ((particles[a].x - particles[b].x) * (particles[a].x - particles[b].x)) + 
                     ((particles[a].y - particles[b].y) * (particles[a].y - particles[b].y))
      
      if (distance < (canvas.value.width/7) * (canvas.value.height/7)) {
        opacityValue = 1 - (distance / 20000)
        ctx.strokeStyle = 'rgba(61, 174, 252,' + opacityValue * 0.15 + ')' // Primary color with low opacity
        ctx.lineWidth = 1
        ctx.beginPath()
        ctx.moveTo(particles[a].x, particles[a].y)
        ctx.lineTo(particles[b].x, particles[b].y)
        ctx.stroke()
      }
    }
  }
}

const animate = () => {
  animationFrameId = requestAnimationFrame(animate)
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)
  
  for (let i = 0; i < particles.length; i++) {
    particles[i].update()
  }
  connect()
}

const handleResize = () => {
  canvas.value.width = window.innerWidth
  canvas.value.height = window.innerHeight
  mouse.radius = ((canvas.value.height/80) * (canvas.value.height/80))
  init()
}

const handleMouseMove = (event) => {
  mouse.x = event.x
  mouse.y = event.y
}

const handleMouseOut = () => {
  mouse.x = undefined
  mouse.y = undefined
}

onMounted(() => {
  ctx = canvas.value.getContext('2d')
  canvas.value.width = window.innerWidth
  canvas.value.height = window.innerHeight
  
  init()
  animate()

  window.addEventListener('resize', handleResize)
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('mouseout', handleMouseOut)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('mouseout', handleMouseOut)
  cancelAnimationFrame(animationFrameId)
})
</script>

<style scoped>
.particle-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1; /* Behind everything */
  pointer-events: none; /* Let clicks pass through */
}
</style>
