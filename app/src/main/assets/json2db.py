import json
import sqlite3
from collections import OrderedDict

from pydantic import BaseModel, Field
from tqdm.auto import tqdm


class Database:
    _instance = None

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super(Database, cls).__new__(cls)
        return cls._instance

    def __init__(self, database: str):
        self.db_name = database

        self.connection = None
        self.cursor = None

        self.connect()

    def connect(self):
        self.connection = sqlite3.connect(self.db_name)
        self.cursor = self.connection.cursor()

    def disconnect(self):
        if self.connection:
            self.cursor.close()
            self.connection.close()

    def execute_query(self, query, params=None):
        self.cursor.execute(query, params or ())
        self.connection.commit()
        return self.cursor.fetchall()

    def create_table(self, table_name, columns):
        column_definitions = ', '.join(columns)
        query = f"CREATE TABLE IF NOT EXISTS {table_name} ({column_definitions})"
        self.execute_query(query)

    def delete_table(self, table_name):
        query = f'DROP TABLE IF EXISTS {table_name}'
        self.execute_query(query)

    def rename_table(self, table_name, new_table_name):
        query = f'ALTER TABLE {table_name} RENAME TO {new_table_name}'
        self.execute_query(query)

    def get_table_columns(self, table_name: str):
        # Запрос для получения списка столбцов таблицы
        self.cursor.execute(f"PRAGMA table_info({table_name})")

        # Извлекаем все строки с информацией о столбцах
        columns = self.cursor.fetchall()

        # Извлекаем имена столбцов из полученных данных
        return [column[1] for column in columns]

    def insert_data(self, table_name: str, data: dict):
        placeholders = ', '.join(['?'] * len(data))
        columns = ', '.join(data.keys())
        query = f"INSERT IGNORE INTO {table_name} ({columns}) VALUES ({placeholders})"
        self.execute_query(query, tuple(data.values()))

    def select_data(self, table_name, columns=None):
        column_names = ', '.join(columns) if columns else '*'
        query = f"SELECT {column_names} FROM {table_name}"
        return self.execute_query(query)

    def update_data(self, table_name: str, data: dict, condition):
        set_values = ', '.join([f"{column} = ?" for column in data.keys()])
        query = f"UPDATE {table_name} SET {set_values} WHERE {condition}"
        self.execute_query(query, tuple(data.values()))

    def delete_data(self, table_name, condition):
        query = f"DELETE FROM {table_name} WHERE {condition}"
        self.execute_query(query)


class Energy(BaseModel):
    caloric: str | None = Field(alias='калорийность')
    squirrels: str | None = Field(alias='белки')
    fats: str | None = Field(alias='жиры')
    carbohydrates: str | None = Field(alias='углеводы')

    def __init__(self, **data):
        super().__init__(**data)
        self.caloric = self.caloric or 0
        self.squirrels = self.squirrels or 0
        self.fats = self.fats or 0
        self.carbohydrates = self.carbohydrates or 0

    def __str__(self):
        return str({
            'калорийность': f"{self.caloric} ККАЛ",
            'белки': f"{self.squirrels} ГРАММ",
            'жиры': f"{self.fats} ГРАММ",
            'углеводы': f"{self.carbohydrates} ГРАММ",
        })


class Recipe(BaseModel):
    name: str = Field(alias='Название')
    url: str = Field(alias='Ссылка на рецепт')
    image: str | None = Field(alias='Изображение')
    time: str = Field(alias='Время приготовления')
    amount: int = Field(alias='Количество порций')
    energy: Energy = Field(alias='Энергетическая ценность на порцию')
    ingredients: dict[str, str] = Field(alias='Ингредиенты')
    description: str | None = Field(alias='Описание')
    instructions: list[str] = Field(alias='Инструкция приготовления')
    advice: str | None = Field(alias='Совет к рецепту')

    def __str__(self):
        return f'{self.name}'


def add_recipes():
    all_recipes_json = json.loads(open('parser_results/all_recipes.json', 'r', encoding='utf-8').read())
    recipes = [Recipe.parse_obj(recipe) for link, recipe in all_recipes_json.items()]

    db = Database('ChefAI.db')

    for recipe in recipes:
        db.insert_data("recipes", OrderedDict({
            "url": recipe.url,
            "name": recipe.name,
            "img": recipe.image,
            "time": recipe.time,
            "amount_servings": recipe.amount,
            "energy": str(recipe.energy),
            "ingredients": str(recipe.ingredients),
            "description": recipe.description,
            "instructions": str(recipe.instructions),
            "advice": recipe.advice,
        }))


def add_recipe_and_category_links():
    """Добавляет связи между рецептами и категориями"""
    recipes_in_categories = json.loads(
        open('parser_results/recipes_from_categories.json', 'r', encoding='utf-8').read())

    db = Database('ChefAI.db')

    updated_recipes = set()
    for name, subcategories in tqdm(recipes_in_categories.items()):
        for category, recipes_roll in tqdm(subcategories.items(), desc=name, leave=True):
            for recipe in recipes_roll:
                recipe_url = recipe['lnk']

                # Добавляем связь категории с рецептом
                db.insert_data("recipes_in_categories", OrderedDict({
                    "recipe_url": recipe_url,
                    "category_name": category
                }))

                # Обновляем информацию о лайках дизлайках и закладках если ещё не сделали этого ранее
                if recipe_url not in updated_recipes:
                    updated_recipes.add(recipe_url)

                    db.update_data("recipes", OrderedDict({
                        "likes": recipe["likes"],
                        "dislikes": recipe["dislikes"],
                        "bookmarks": recipe["bookmarks"],
                    }), f"url = '{recipe_url}'")

                # else:
                #     updated_recipes[recipe_url].append(category)


def add_categories():
    all_categories = json.loads(open('parser_results/all_categories.json', 'r', encoding='utf-8').read())

    db = Database('ChefAI.db')

    for category_name, category_data in all_categories.items():
        category_link, subcategories = category_data['lnk'], category_data['subcategories']

        # Добавляем родительскую категорию
        db.insert_data("categories", OrderedDict({
            "name": category_name,
            "url": category_link,
        }))

        # Добавляем все дочерние категории
        for category, category_link in subcategories.items():
            db.insert_data("categories", OrderedDict({
                "name": category,
                "url": category_link,
                "parent": category_name,
            }))


def delete_duplicates(db_file, table_name):
    db = Database(db_file)

    unique_columns = db.get_table_columns(table_name)

    # Создаем временную таблицу для хранения уникальных записей
    temp_table_name = f'{table_name}_temp'
    unique_columns_str = ', '.join(unique_columns)
    create_temp_table_query = f'''
            CREATE TABLE {temp_table_name} AS
            SELECT MIN(rowid) AS row_id, {unique_columns_str}
            FROM {table_name}
            GROUP BY {unique_columns_str}
        '''
    db.execute_query(create_temp_table_query)

    # Удаляем исходную таблицу
    db.delete_table(table_name)

    # Переименовываем временную таблицу в исходное имя
    db.rename_table(temp_table_name, table_name)


def main():
    # add_categories()
    # add_recipe_and_category_links()
    delete_duplicates("ChefAI.db", "recipes_in_categories")
    pass


if __name__ == '__main__':
    main()
