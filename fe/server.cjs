const jsonServer = require('json-server');
const server = jsonServer.create();
const router = jsonServer.router('db.json');
const middlewares = jsonServer.defaults();

server.use(middlewares);
server.use(jsonServer.bodyParser);

server.post('/login', (req, res) => {
  const { email, password } = req.body;
  const users = router.db.get('users').value();
  const user = users.find(u => u.email === email && u.password === password);
  if (user) {
    res.status(200).json({
      id: user.id,
      email: user.email,
      message: 'Đăng nhập thành công!'
    });
  } else {
    res.status(401).json({ message: 'Sai tài khoản hoặc mật khẩu' });
  }
});

server.use(router);

const PORT = 3001;
server.listen(PORT, () => {
  console.log(`JSON Server is running on port ${PORT}`);
}); 