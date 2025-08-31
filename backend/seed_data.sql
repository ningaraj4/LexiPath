-- LexiPath Seed Data
-- Demo profiles and sample data for development and testing

-- Insert demo users (these would normally be created via Firebase Auth)
-- Note: In production, users are created automatically when they first authenticate

-- Demo profiles for different learning tracks
INSERT INTO profiles (id, user_id, goal_type, language_level, target_language, native_language, industry, created_at, updated_at) VALUES
-- Bilingual Language Track Users
('550e8400-e29b-41d4-a716-446655440001', 'demo-user-1', 'bilingual_language_track', 'beginner', 'Spanish', 'English', NULL, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440002', 'demo-user-2', 'bilingual_language_track', 'intermediate', 'French', 'English', NULL, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440003', 'demo-user-3', 'bilingual_language_track', 'advanced', 'German', 'English', NULL, NOW(), NOW()),

-- Monolingual Industry Track Users
('550e8400-e29b-41d4-a716-446655440004', 'demo-user-4', 'monolingual_industry_track', 'intermediate', 'English', 'English', 'Technology', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440005', 'demo-user-5', 'monolingual_industry_track', 'advanced', 'English', 'English', 'Finance', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440006', 'demo-user-6', 'monolingual_industry_track', 'beginner', 'English', 'English', 'Healthcare', NOW(), NOW());

-- Sample daily content for the past week
INSERT INTO daily_content (id, user_id, word, meaning, pronunciation, date, examples_target, examples_native, created_at) VALUES
-- Spanish Learning Content (demo-user-1)
('content-001', 'demo-user-1', 'serendipidad', 'La ocurrencia de eventos por casualidad de manera feliz', '/se.ɾen.diˈpi.dad/', '2024-01-15', 
 '["Fue pura serendipidad que nos encontráramos en el café.", "El descubrimiento fue resultado de la serendipidad."]',
 '["It was pure serendipity that we met at the café.", "The discovery was a result of serendipity."]', NOW()),

('content-002', 'demo-user-1', 'efímero', 'Que dura poco tiempo; pasajero', '/e.ˈfi.me.ɾo/', '2024-01-14',
 '["La belleza de las flores es efímera.", "Sus momentos de felicidad fueron efímeros."]',
 '["The beauty of flowers is ephemeral.", "Their moments of happiness were fleeting."]', NOW()),

('content-003', 'demo-user-1', 'resiliente', 'Capaz de recuperarse rápidamente de las dificultades', '/re.si.ˈljen.te/', '2024-01-13',
 '["Ella es muy resiliente ante los desafíos.", "La comunidad mostró ser resiliente después del desastre."]',
 '["She is very resilient in the face of challenges.", "The community proved to be resilient after the disaster."]', NOW()),

-- French Learning Content (demo-user-2)
('content-004', 'demo-user-2', 'sérendipité', 'Découverte fortuite et heureuse', '/se.ʁɑ̃.di.pi.te/', '2024-01-15',
 '["C''était de la pure sérendipité de vous rencontrer ici.", "Cette découverte fut le fruit de la sérendipité."]',
 '["It was pure serendipity to meet you here.", "This discovery was the result of serendipity."]', NOW()),

('content-005', 'demo-user-2', 'éphémère', 'Qui ne dure qu''un temps très court', '/e.fe.mɛʁ/', '2024-01-14',
 '["La beauté des fleurs est éphémère.", "Ces moments de bonheur furent éphémères."]',
 '["The beauty of flowers is ephemeral.", "These moments of happiness were fleeting."]', NOW()),

-- Technology Industry Content (demo-user-4)
('content-006', 'demo-user-4', 'scalability', 'The ability of a system to handle increased load', '/ˌskeɪləˈbɪlɪti/', '2024-01-15',
 '["The application''s scalability was tested under heavy load.", "We need to consider scalability in our architecture design."]',
 '["The application''s scalability was tested under heavy load.", "We need to consider scalability in our architecture design."]', NOW()),

('content-007', 'demo-user-4', 'microservices', 'Architectural approach of building applications as independent services', '/ˈmaɪkroʊˌsɜrvɪsɪz/', '2024-01-14',
 '["Our team decided to migrate to a microservices architecture.", "Microservices allow for better scalability and maintainability."]',
 '["Our team decided to migrate to a microservices architecture.", "Microservices allow for better scalability and maintainability."]', NOW()),

-- Finance Industry Content (demo-user-5)
('content-008', 'demo-user-5', 'arbitrage', 'Simultaneous buying and selling to profit from price differences', '/ˈɑrbɪtrɑʒ/', '2024-01-15',
 '["The trader identified an arbitrage opportunity between markets.", "Arbitrage helps ensure market efficiency."]',
 '["The trader identified an arbitrage opportunity between markets.", "Arbitrage helps ensure market efficiency."]', NOW()),

('content-009', 'demo-user-5', 'derivatives', 'Financial instruments whose value depends on underlying assets', '/dɪˈrɪvətɪvz/', '2024-01-14',
 '["The bank''s derivatives portfolio requires careful risk management.", "Derivatives can be used for hedging or speculation."]',
 '["The bank''s derivatives portfolio requires careful risk management.", "Derivatives can be used for hedging or speculation."]', NOW());

-- Sample quiz logs with varying performance
INSERT INTO quiz_logs (id, user_id, content_id, quiz_type, user_answer, correct_answer, is_correct, time_spent_ms, created_at) VALUES
-- demo-user-1 quiz history
('quiz-001', 'demo-user-1', 'content-001', 'mcq', 'La ocurrencia de eventos por casualidad de manera feliz', 'La ocurrencia de eventos por casualidad de manera feliz', true, 4500, NOW() - INTERVAL '1 hour'),
('quiz-002', 'demo-user-1', 'content-002', 'fill_blank', 'efímero', 'efímero', true, 3200, NOW() - INTERVAL '1 day'),
('quiz-003', 'demo-user-1', 'content-003', 'mcq', 'Capaz de adaptarse', 'Capaz de recuperarse rápidamente de las dificultades', false, 6800, NOW() - INTERVAL '2 days'),

-- demo-user-2 quiz history
('quiz-004', 'demo-user-2', 'content-004', 'situation', 'C''était de la pure sérendipité', 'C''était de la pure sérendipité de vous rencontrer', true, 5500, NOW() - INTERVAL '2 hours'),
('quiz-005', 'demo-user-2', 'content-005', 'mcq', 'Qui ne dure qu''un temps très court', 'Qui ne dure qu''un temps très court', true, 2800, NOW() - INTERVAL '1 day'),

-- demo-user-4 quiz history
('quiz-006', 'demo-user-4', 'content-006', 'mcq', 'The ability of a system to handle increased load', 'The ability of a system to handle increased load', true, 3500, NOW() - INTERVAL '30 minutes'),
('quiz-007', 'demo-user-4', 'content-007', 'fill_blank', 'microservices', 'microservices', true, 4200, NOW() - INTERVAL '1 day');

-- Sample mastery scores
INSERT INTO mastery_scores (id, user_id, word_id, mastery_score, last_practiced, practice_count, correct_count, created_at, updated_at) VALUES
-- demo-user-1 mastery
('mastery-001', 'demo-user-1', 'serendipidad', 75, NOW() - INTERVAL '1 hour', 3, 2, NOW() - INTERVAL '3 days', NOW()),
('mastery-002', 'demo-user-1', 'efímero', 85, NOW() - INTERVAL '1 day', 4, 4, NOW() - INTERVAL '4 days', NOW()),
('mastery-003', 'demo-user-1', 'resiliente', 45, NOW() - INTERVAL '2 days', 2, 1, NOW() - INTERVAL '2 days', NOW()),

-- demo-user-2 mastery
('mastery-004', 'demo-user-2', 'sérendipité', 80, NOW() - INTERVAL '2 hours', 2, 2, NOW() - INTERVAL '1 day', NOW()),
('mastery-005', 'demo-user-2', 'éphémère', 90, NOW() - INTERVAL '1 day', 3, 3, NOW() - INTERVAL '2 days', NOW()),

-- demo-user-4 mastery
('mastery-006', 'demo-user-4', 'scalability', 70, NOW() - INTERVAL '30 minutes', 2, 2, NOW() - INTERVAL '1 day', NOW()),
('mastery-007', 'demo-user-4', 'microservices', 65, NOW() - INTERVAL '1 day', 3, 2, NOW() - INTERVAL '2 days', NOW()),

-- demo-user-5 mastery
('mastery-008', 'demo-user-5', 'arbitrage', 95, NOW() - INTERVAL '1 hour', 5, 5, NOW() - INTERVAL '1 week', NOW()),
('mastery-009', 'demo-user-5', 'derivatives', 88, NOW() - INTERVAL '1 day', 4, 4, NOW() - INTERVAL '1 week', NOW());

-- Sample weekly plans
INSERT INTO weekly_plans (id, user_id, week_start, focus_areas, daily_goals, review_words, created_at) VALUES
-- Current week plan for demo-user-1 (Spanish learner)
('plan-001', 'demo-user-1', '2024-01-14', 
 '["Basic conversation vocabulary", "Past tense verbs", "Common adjectives"]',
 '{"monday": "Learn 3 past tense verbs", "tuesday": "Practice descriptive adjectives", "wednesday": "Conversation practice", "thursday": "Review week vocabulary", "friday": "Apply in context", "saturday": "Mixed practice", "sunday": "Weekly review"}',
 '["resiliente", "efímero", "serendipidad"]', NOW()),

-- Current week plan for demo-user-2 (French learner)
('plan-002', 'demo-user-2', '2024-01-14',
 '["Advanced vocabulary", "Subjunctive mood", "Literary expressions"]',
 '{"monday": "Master subjunctive forms", "tuesday": "Literary vocabulary", "wednesday": "Complex sentence structures", "thursday": "Review and practice", "friday": "Reading comprehension", "saturday": "Writing practice", "sunday": "Weekly assessment"}',
 '["éphémère", "sérendipité"]', NOW()),

-- Current week plan for demo-user-4 (Tech professional)
('plan-003', 'demo-user-4', '2024-01-14',
 '["Cloud computing terms", "DevOps vocabulary", "System architecture"]',
 '{"monday": "Cloud service terminology", "tuesday": "DevOps pipeline vocabulary", "wednesday": "Architecture patterns", "thursday": "Performance metrics", "friday": "Security terminology", "saturday": "Integration practice", "sunday": "Technical review"}',
 '["scalability", "microservices"]', NOW()),

-- Current week plan for demo-user-5 (Finance professional)
('plan-004', 'demo-user-5', '2024-01-14',
 '["Risk management", "Investment strategies", "Market analysis"]',
 '{"monday": "Risk assessment terminology", "tuesday": "Portfolio management", "wednesday": "Market indicators", "thursday": "Regulatory vocabulary", "friday": "Financial modeling", "saturday": "Case study analysis", "sunday": "Weekly market review"}',
 '["arbitrage", "derivatives"]', NOW());

-- Additional historical content for content history testing
INSERT INTO daily_content (id, user_id, word, meaning, pronunciation, date, examples_target, examples_native, created_at) VALUES
-- More Spanish content for demo-user-1
('content-010', 'demo-user-1', 'perspicaz', 'Que tiene agudeza mental para entender las cosas', '/per.spi.ˈkas/', '2024-01-12',
 '["Es una persona muy perspicaz en los negocios.", "Su análisis perspicaz del problema fue valioso."]',
 '["She is a very perceptive person in business.", "His insightful analysis of the problem was valuable."]', NOW() - INTERVAL '3 days'),

('content-011', 'demo-user-1', 'meticuloso', 'Que actúa con mucho cuidado y atención al detalle', '/me.ti.ku.ˈlo.so/', '2024-01-11',
 '["Su trabajo meticuloso impresionó a todos.", "Necesitamos ser meticulosos en esta investigación."]',
 '["His meticulous work impressed everyone.", "We need to be meticulous in this investigation."]', NOW() - INTERVAL '4 days'),

-- More tech content for demo-user-4
('content-012', 'demo-user-4', 'containerization', 'Technology for packaging applications with their dependencies', '/kənˌteɪnərɪˈzeɪʃən/', '2024-01-13',
 '["Containerization simplifies application deployment.", "Docker is a popular containerization platform."]',
 '["Containerization simplifies application deployment.", "Docker is a popular containerization platform."]', NOW() - INTERVAL '2 days'),

('content-013', 'demo-user-4', 'orchestration', 'Automated management of containerized applications', '/ˌɔrkɪˈstreɪʃən/', '2024-01-12',
 '["Kubernetes provides container orchestration capabilities.", "Orchestration tools help manage complex deployments."]',
 '["Kubernetes provides container orchestration capabilities.", "Orchestration tools help manage complex deployments."]', NOW() - INTERVAL '3 days');

-- Cache some content in Redis (this would normally be done by the application)
-- Note: Redis commands would be executed separately, this is just for reference
/*
Redis commands to populate cache:
SETEX "daily_content:demo-user-1:2024-01-15" 86400 '{"id":"content-001","word":"serendipidad","meaning":"La ocurrencia de eventos por casualidad de manera feliz","date":"2024-01-15"}'
SETEX "daily_content:demo-user-2:2024-01-15" 86400 '{"id":"content-004","word":"sérendipité","meaning":"Découverte fortuite et heureuse","date":"2024-01-15"}'
SETEX "daily_content:demo-user-4:2024-01-15" 86400 '{"id":"content-006","word":"scalability","meaning":"The ability of a system to handle increased load","date":"2024-01-15"}'
*/

-- Update sequences to avoid conflicts
SELECT setval('profiles_id_seq', (SELECT MAX(id) FROM profiles WHERE id ~ '^[0-9]+$')::bigint);
SELECT setval('daily_content_id_seq', (SELECT MAX(id) FROM daily_content WHERE id ~ '^[0-9]+$')::bigint);
SELECT setval('quiz_logs_id_seq', (SELECT MAX(id) FROM quiz_logs WHERE id ~ '^[0-9]+$')::bigint);
SELECT setval('mastery_scores_id_seq', (SELECT MAX(id) FROM mastery_scores WHERE id ~ '^[0-9]+$')::bigint);
SELECT setval('weekly_plans_id_seq', (SELECT MAX(id) FROM weekly_plans WHERE id ~ '^[0-9]+$')::bigint);
