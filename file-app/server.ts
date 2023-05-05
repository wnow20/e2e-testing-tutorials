import express, { Express, Request, Response } from 'express';
import multer from 'multer'

const storage = multer.diskStorage({
  destination: 'uploads/',
  filename: function (req, file, cb) {
    cb(null, file.originalname)
  }
})

const upload = multer({ storage })

const app: Express = express()

app.use(express.static('dist'))
app.use('/files', express.static('uploads'))

app.get('/echo', (req, res) => {
  res.send('Hello World!')
})

app.post('/files', upload.single('file'), (req: Request, res) => {
  res.json({
    success: 'ok',
    data: req.file.path,
  })
})

const port = 3000
app.listen(port, () => {
  console.log(`Example app listening on port ${port}`)
})
