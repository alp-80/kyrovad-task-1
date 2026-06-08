const express = require('express');
const app = express();
app.use(express.json());

// Данные
const groups = [
    { groupId: 1, groupName: "2307а" },
    { groupId: 2, groupName: "2307б" },
    { groupId: 3, groupName: "2307в" },
    { groupId: 4, groupName: "2307г" }
];

const users = [
    { userId: 1, login: "test", email: "test@test.com", phoneNumber: "+79991234567", person: null }
];

let tokens = {};

// API эндпоинты
app.post('/api/auth/login', (req, res) => {
    const { login, password } = req.body;
    if (login === "test" && password === "test") {
        const token = "test-token-123";
        tokens[token] = login;
        res.json({ token: token });
    } else {
        res.status(401).json({ error: "Неверный логин или пароль" });
    }
});

app.post('/api/auth/register', (req, res) => {
    const { login, password, email, phoneNumber, person } = req.body;
    const newUser = {
        userId: users.length + 1,
        login,
        email,
        phoneNumber,
        person
    };
    users.push(newUser);
    res.status(200).send();
});

app.get('/api/users', (req, res) => {
    const auth = req.headers.authorization;
    if (!auth || !auth.startsWith('Bearer ')) {
        return res.status(401).json({ error: "Unauthorized" });
    }
    res.json(users);
});

app.get('/api/groups', (req, res) => {
    res.json(groups);
});

app.listen(8080, () => {
    console.log('Сервер запущен на http://localhost:8080');
    console.log('Тестовый вход: login=test, password=test');
});