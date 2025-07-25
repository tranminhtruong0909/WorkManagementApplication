import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Login from './pages/Login'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        {/* Có thể thêm các route khác tại đây */}
      </Routes>
    </Router>
  )
}

export default App
