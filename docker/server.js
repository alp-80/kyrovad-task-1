const express = require('express');
const { Pool } = require('pg');
const app = express();
app.use(express.json());

// CORS для Android
app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    if (req.method === 'OPTIONS') {
        return res.sendStatus(200);
    }
    next();
});

// Проверочный маршрут
app.get('/', (req, res) => {
    res.json({ message: 'Сервер работает! Используйте /api/... для запросов' });
});

// Подключение к PostgreSQL
const pool = new Pool({
    host: 'database',
    port: 5432,
    user: 'postgres',
    password: 'secret',
    database: 'depositdb',
});

// Создание таблиц при запуске
async function initDB() {
    await pool.query(`
        CREATE TABLE IF NOT EXISTS groups (
            id SERIAL PRIMARY KEY,
            name VARCHAR(100) NOT NULL UNIQUE
        )
    `);
    await pool.query(`
        CREATE TABLE IF NOT EXISTS users (
            id SERIAL PRIMARY KEY,
            login VARCHAR(100) UNIQUE NOT NULL,
            password VARCHAR(100) NOT NULL,
            email VARCHAR(100),
            phone VARCHAR(100),
            first_name VARCHAR(100),
            last_name VARCHAR(100),
            middle_name VARCHAR(100),
            birth_date VARCHAR(50),
            gender VARCHAR(10),
            group_id INTEGER REFERENCES groups(id)
        )
    `);

    // Добавляем группы по умолчанию
    const groups = ['2307а', '2307б', '2307в', '2307г'];
    for (const name of groups) {
        await pool.query('INSERT INTO groups (name) VALUES ($1) ON CONFLICT (name) DO NOTHING', [name]);
    }

    // Добавляем тестового пользователя
    await pool.query(`
        INSERT INTO users (login, password, email, phone, first_name, last_name, middle_name, birth_date, gender, group_id)
        VALUES ('test', '123', 'test@example.com', '+79991234567', 'Тест', 'Тестовый', 'Тестович', '2000-01-01', 'MALE', 1)
        ON CONFLICT (login) DO NOTHING
    `);

    console.log('База данных инициализирована');
}

// Получение списка групп
app.get('/api/groups', async (req, res) => {
    try {
        const result = await pool.query('SELECT id as "groupId", name as "groupName" FROM groups');
        res.json(result.rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Регистрация
app.post('/api/auth/register', async (req, res) => {
    const { login, password, email, phoneNumber, person } = req.body;
    const { firstName, lastName, middleName, birthDate, gender, groupId } = person || {};

    try {
        await pool.query(
            `INSERT INTO users
             (login, password, email, phone, first_name, last_name, middle_name, birth_date, gender, group_id)
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)`,
            [login, password, email, phoneNumber, firstName, lastName, middleName, birthDate, gender, groupId]
        );
        res.status(200).json({ message: 'Регистрация успешна' });
    } catch (err) {
        if (err.code === '23505') {
            res.status(409).json({ error: 'Пользователь уже существует' });
        } else {
            res.status(500).json({ error: 'Ошибка сервера' });
        }
    }
});

// Вход
app.post('/api/auth/login', async (req, res) => {
    const { login, password } = req.body;
    try {
        const result = await pool.query(
            'SELECT id, login FROM users WHERE login = $1 AND password = $2',
            [login, password]
        );

        if (result.rows.length > 0) {
            const user = result.rows[0];
            // Генерируем токен с userId
            const payload = JSON.stringify({
                userId: user.id,
                login: user.login,
                exp: Date.now() + 86400000
            });
            const token = Buffer.from(payload).toString('base64');
            res.json({ token });
        } else {
            res.status(401).json({ error: 'Неверный логин или пароль' });
        }
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Получение информации о текущем пользователе
app.get('/api/me', async (req, res) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ error: 'Не авторизован' });
    }

    try {
        const token = authHeader.substring(7);
        const decoded = JSON.parse(Buffer.from(token, 'base64').toString());
        const userId = decoded.userId;

        const result = await pool.query(
            `SELECT id as "userId", login, email, phone as "phoneNumber",
                    jsonb_build_object(
                        'firstName', first_name,
                        'lastName', last_name,
                        'middleName', middle_name,
                        'birthDate', birth_date,
                        'gender', gender,
                        'groupId', group_id
                    ) as person
             FROM users WHERE id = $1`,
            [userId]
        );

        if (result.rows.length > 0) {
            res.json(result.rows[0]);
        } else {
            res.status(404).json({ error: 'Пользователь не найден' });
        }
    } catch (err) {
        res.status(401).json({ error: 'Недействительный токен' });
    }
});

// Получение списка пользователей
app.get('/api/users', async (req, res) => {
    try {
        const result = await pool.query(`
            SELECT
                id as "userId",
                login,
                email,
                phone as "phoneNumber",
                jsonb_build_object(
                    'firstName', first_name,
                    'lastName', last_name,
                    'middleName', middle_name,
                    'birthDate', birth_date,
                    'gender', gender,
                    'groupId', group_id
                ) as person
            FROM users
        `);
        res.json(result.rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Запуск сервера
const PORT = process.env.PORT || 8080;
app.listen(PORT, '0.0.0.0', async () => {
    await initDB();
    console.log(`Сервер запущен на порту ${PORT}`);
    console.log(`Для тестов: логин "test", пароль "123"`);
});