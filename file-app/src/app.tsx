import * as React from 'react'
import { createRoot } from 'react-dom/client'

interface AppProps {
}

const App: React.FC<AppProps> = () => (
  <div>
    <form action="/files" method="POST" encType="multipart/form-data">
      <p>
        <input type="file" name="file" id="file" />
      </p>
      <p><input type="submit" value="提交" /></p>
    </form>
  </div>
)

const domNode = document.querySelector('.rootContainer')

const root = createRoot(domNode!)
root.render(<App />)
