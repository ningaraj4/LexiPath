-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create custom types
CREATE TYPE goal_type AS ENUM ('language', 'industry');
CREATE TYPE language_level AS ENUM ('beginner', 'intermediate', 'advanced');
CREATE TYPE quiz_type AS ENUM ('mcq', 'fill_blank', 'situation');

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    firebase_uid VARCHAR(128) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Profiles table
CREATE TABLE profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    goal_type goal_type NOT NULL,
    target_lang VARCHAR(10),
    base_lang VARCHAR(10),
    level language_level NOT NULL,
    industry_sector VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT check_language_goal CHECK (
        (goal_type = 'language' AND target_lang IS NOT NULL AND base_lang IS NOT NULL) OR
        (goal_type = 'industry' AND industry_sector IS NOT NULL)
    )
);

-- Daily content table
CREATE TABLE daily_content (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    word VARCHAR(255) NOT NULL,
    meaning TEXT NOT NULL,
    examples_target TEXT[] NOT NULL DEFAULT '{}',
    examples_base TEXT[] DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    UNIQUE(user_id, date)
);

-- Quiz logs table
CREATE TABLE quiz_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_id UUID NOT NULL REFERENCES daily_content(id) ON DELETE CASCADE,
    quiz_type quiz_type NOT NULL,
    question TEXT NOT NULL,
    options TEXT[] DEFAULT '{}',
    correct_answer TEXT NOT NULL,
    user_answer TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Mastery table
CREATE TABLE mastery (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_id UUID NOT NULL REFERENCES daily_content(id) ON DELETE CASCADE,
    mastery_score INTEGER NOT NULL CHECK (mastery_score >= 0 AND mastery_score <= 100),
    next_review_date DATE,
    last_reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    UNIQUE(user_id, content_id)
);

-- Weekly plans table
CREATE TABLE weekly_plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    week_start DATE NOT NULL,
    plan JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    UNIQUE(user_id, week_start)
);

-- Indexes for performance
CREATE INDEX idx_users_firebase_uid ON users(firebase_uid);
CREATE INDEX idx_profiles_user_id ON profiles(user_id);
CREATE INDEX idx_daily_content_user_date ON daily_content(user_id, date);
CREATE INDEX idx_quiz_logs_user_content ON quiz_logs(user_id, content_id);
CREATE INDEX idx_quiz_logs_created_at ON quiz_logs(created_at);
CREATE INDEX idx_mastery_user_content ON mastery(user_id, content_id);
CREATE INDEX idx_mastery_next_review ON mastery(user_id, next_review_date) WHERE next_review_date IS NOT NULL;
CREATE INDEX idx_weekly_plans_user_week ON weekly_plans(user_id, week_start);

-- Updated at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply updated_at triggers
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_profiles_updated_at BEFORE UPDATE ON profiles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_mastery_updated_at BEFORE UPDATE ON mastery FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
