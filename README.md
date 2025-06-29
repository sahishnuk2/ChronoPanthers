How to run this code:

Step 1 - Git clone the project

Step 2 - Rename .env.example to .env

Step 3 - Sign up for Supabase and Create a Database (Remember your password)

Step 4 - Create the needed tables by copying the sql codes provided after the instructions.

Step 5 - Click Connect at the top of the Supabase page, then select JDBC as the Type

Step 6 - Copy and paste the Transaction Pooler URL and replace the [Your JDBC Supabase URI here] with the URL

Step 7 - Replace your password into the URL

Step 8 - Click into ChronoPanthers.java and Run

---Users Table---
-- Create the loginDetails table
CREATE TABLE loginDetails (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    workSessions INTEGER DEFAULT 0,
    breakSessions INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Create index for faster username lookups
CREATE INDEX idx_loginDetails_username ON loginDetails(username);

-- Insert some test data (optional)
INSERT INTO loginDetails (username, password, workSessions, breakSessions) 
VALUES ('testuser', 'testpass', 0, 0);

---TaskList Table---
-- Create the tasks table
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    task_name VARCHAR(500) NOT NULL,
    task_type VARCHAR(50) NOT NULL, -- 'Normal' or 'Deadline'
    priority VARCHAR(20) NOT NULL, -- 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'NONE'
    is_completed BOOLEAN DEFAULT FALSE,
    due_date DATE NULL, -- Only for deadline tasks
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Create index for faster username lookups
CREATE INDEX idx_tasks_username ON tasks(username);

-- Create index for faster task filtering
CREATE INDEX idx_tasks_completed ON tasks(is_completed);

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = TIMEZONE('utc'::text, NOW());
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_tasks_updated_at 
    BEFORE UPDATE ON tasks 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Insert some sample data (optional)
INSERT INTO tasks (username, task_name, task_type, priority, due_date) VALUES
('testuser', 'Complete project report', 'Deadline', 'HIGH', '2025-07-15'),
('testuser', 'Review code', 'Normal', 'MEDIUM', NULL),
('testuser', 'Prepare presentation', 'Deadline', 'CRITICAL', '2025-07-01');

NUS Orbital 2025 - Milestone 1

Proposed Level of Achievement: 
Project Gemini 

Motivation: 
Many students and professionals struggle with time management and procrastination. The Pomodoro Technique is a proven method that enhances focus and productivity by breaking work into intervals. However, most existing Pomodoro apps lack integration with task management and progress tracking.

Our application will seamlessly integrate a Pomodoro Timer, To-Do List, and Productivity Analysis to help users stay on track, measure their progress, and optimize their workflow.

Aim: 
We hope to develop a user-friendly Java Application that helps users to stay focused using the Pomodoro technique (25 min work / 5 min rest cycles), organise tasks with a simple To-Do List and track productivity with insights on completed Pomodoro sessions

User Stories: 
As a student, I want to use a Pomodoro Timer to break study sessions into focused intervals, so I can stay productive without burnout.
As a working professional, I want a To-Do List alongside my Pomodoro Timer, so I can manage tasks efficiently.
As a student/working professional, I want to track my completed Pomodoro sessions, so I can analyze my productivity patterns and improve myself.

Scope of Project: 
The Web App provides a Pomodoro Timer Interface for students to time themselves and plan their study for optimal efficiency. 

Features by Milestone 1:
Pomodoro Timer - (By Milestone 1 (2 June))
25 min work / 5 min rest cycles - configurable
Start, Pause and Reset functionality

NUS Orbital 2025 -Technical Proof of Concept

Basic Pomodoro Timer Implementation (13 May - 19 May)
Create timer functionality with 25/5 minute work/break cycles
Implement Start/Pause/Reset functionality
Design simple GUI with timer display and control buttons
Initial Integration Testing (20 May - 26 May)
Connect timer logic with GUI components
Test basic timer functionality and user interactions
Fix bugs and refine user interface
 
Documentation and Milestone 1 Preparation (27 May - 2 Jun)
Document code with comments and basic Javadoc
Prepare demonstration for Milestone 1 evaluation
Set up GitHub repository with initial commit

Tech Stack 

JavaFX (FrontEnd)

SQLite (Backend) 

How are we different from similar platforms? 

Task Management
We provide users with the ability to add their tasks into our task management section which helps to prioritise more important tasks and increase users’ efficiency.

Productivity Tracking
The app tracks the productivity of the user by tracking how many breaks and work session the user does.

Development Plan 
2rd week of May: Finalized pitch for Orbital Lift-off 
3rd week of May: Pick up necessary technologies - JavaFX, CSS, SQLite, Git
4th week of May: Implement Timer Logic with a user Interface. Add in a Login and Sign Up page.
1st and 2nd week of June: Task Management and Persistent Storage
3rd week of June: Design Task Management using CSS
4th week of June: Testing and debugging 
1st and 2nd week of July: Implementation of peer teams’ suggestions as well as AI integration
3rd week of July: Testing and debugging
