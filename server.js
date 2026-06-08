const express = require('express');
const { Pool } = require('pg');
const app = express();
app.use(express.json());

// Подключение к PostgreSQL
const pool = new Pool({
    host: 'database',
    port: 5432,
    user: 'postgres',
    password: 'secret',
    database: 'mydb',
});

// Создание таблиц при запуске
async function initDB() {
    await pool.query(`
        CREATE TABLE IF NOT EXISTS groups (
            id SERIAL PRIMARY KEY,
            name VARCHAR(100) NOT NULL
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
            group_id INTEGER
        )
    `);

    // Добавляем группы по умолчанию
    const groups = ['2307а', '2307б', '2307в', '2307г'];
    for (const name of groups) {
        await pool.query('INSERT INTO groups (name) VALUES ($1) ON CONFLICT DO NOTHING', [name]);
    }

    console.log('База данных инициализирована');
}

// Получение списка групп
app.get('/api/groups', async (req, res) => {
    const result = await pool.query('SELECT id as "groupId", name as "groupName" FROM groups');
    res.json(result.rows);
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
        res.status(200).send();
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
    const result = await pool.query('SELECT * FROM users WHERE login = $1 AND password = $2', [login, password]);

    if (result.rows.length > 0) {
        const token = 'token-' + Date.now() + '-' + Math.random();
        res.json({ token });
    } else {
        res.status(401).json({ error: 'Неверный логин или пароль' });
    }
});

// Получение списка пользователей
app.get('/api/users', async (req, res) => {
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
});

// Запуск сервера
app.listen(8080, async () => {
    await initDB();
    console.log('Сервер запущен на порту 8080');
    console.log('Для входа зарегистрируйтесь через приложение');
});