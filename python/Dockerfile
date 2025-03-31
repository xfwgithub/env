FROM python:3.11-slim

WORKDIR /app

# 安装Poetry
RUN pip install poetry==1.6.1 \
    && poetry config virtualenvs.create false

# 安装依赖
COPY pyproject.toml poetry.lock* ./
RUN poetry install --no-dev --no-interaction --no-ansi

# 复制项目文件
COPY . .

# 设置环境变量
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PYTHONPATH="/app:${PYTHONPATH}"

# 默认命令
CMD ["python", "-m", "project_name"] 