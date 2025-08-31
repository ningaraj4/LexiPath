-- Drop triggers
DROP TRIGGER IF EXISTS update_mastery_updated_at ON mastery;
DROP TRIGGER IF EXISTS update_profiles_updated_at ON profiles;
DROP TRIGGER IF EXISTS update_users_updated_at ON users;

-- Drop function
DROP FUNCTION IF EXISTS update_updated_at_column();

-- Drop indexes
DROP INDEX IF EXISTS idx_weekly_plans_user_week;
DROP INDEX IF EXISTS idx_mastery_next_review;
DROP INDEX IF EXISTS idx_mastery_user_content;
DROP INDEX IF EXISTS idx_quiz_logs_created_at;
DROP INDEX IF EXISTS idx_quiz_logs_user_content;
DROP INDEX IF EXISTS idx_daily_content_user_date;
DROP INDEX IF EXISTS idx_profiles_user_id;
DROP INDEX IF EXISTS idx_users_firebase_uid;

-- Drop tables in reverse order
DROP TABLE IF EXISTS weekly_plans;
DROP TABLE IF EXISTS mastery;
DROP TABLE IF EXISTS quiz_logs;
DROP TABLE IF EXISTS daily_content;
DROP TABLE IF EXISTS profiles;
DROP TABLE IF EXISTS users;

-- Drop custom types
DROP TYPE IF EXISTS quiz_type;
DROP TYPE IF EXISTS language_level;
DROP TYPE IF EXISTS goal_type;

-- Drop extension
DROP EXTENSION IF EXISTS "uuid-ossp";
