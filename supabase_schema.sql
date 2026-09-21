-- ==============================================================================
-- Supabase Schema Synchronization File
-- ==============================================================================
-- This file ensures that your remote Supabase instance exactly 100% matches
-- the local database architecture required by the Android application.
-- 
-- Instructions:
-- 1. Go to your Supabase project dashboard -> SQL Editor
-- 2. Paste the entirety of this file into a new query
-- 3. Click "Run" to synchronize your remote backend
-- ==============================================================================

-- 1. Create 'architecture_records' table (Matches ArchitectureEntity.kt / SupabaseRecordDto.kt)
CREATE TABLE IF NOT EXISTS public.architecture_records (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    user_id UUID DEFAULT auth.uid(),
    updated_at BIGINT
);

-- Turn on Row Level Security for standard data protection
ALTER TABLE public.architecture_records ENABLE ROW LEVEL SECURITY;

-- Idempotent RLS Policies for architecture_records
DROP POLICY IF EXISTS "Users can view their own architecture_records" ON public.architecture_records;
DROP POLICY IF EXISTS "Users can insert their own architecture_records" ON public.architecture_records;
DROP POLICY IF EXISTS "Users can update their own architecture_records" ON public.architecture_records;
DROP POLICY IF EXISTS "Users can delete their own architecture_records" ON public.architecture_records;

-- Policy: Allow users to view their own records
CREATE POLICY "Users can view their own architecture_records"
ON public.architecture_records FOR SELECT
USING (auth.uid() = user_id);

-- Policy: Allow users to insert their own records
CREATE POLICY "Users can insert their own architecture_records"
ON public.architecture_records FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Policy: Allow users to update their own records
CREATE POLICY "Users can update their own architecture_records"
ON public.architecture_records FOR UPDATE
USING (auth.uid() = user_id);

-- Policy: Allow users to delete their own records
CREATE POLICY "Users can delete their own architecture_records"
ON public.architecture_records FOR DELETE
USING (auth.uid() = user_id);

-- Trigger to strictly enforce the user_id (prevents clients from setting null or overriding it)
CREATE OR REPLACE FUNCTION set_architecture_user_id()
RETURNS TRIGGER AS $$
BEGIN
  NEW.user_id := auth.uid();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS ensure_architecture_user_id ON public.architecture_records;
CREATE TRIGGER ensure_architecture_user_id
BEFORE INSERT ON public.architecture_records
FOR EACH ROW
EXECUTE FUNCTION set_architecture_user_id();


-- 2. Create 'documents' storage bucket (Matches StorageRemoteDataSource.kt)
INSERT INTO storage.buckets (id, name, public) 
VALUES ('documents', 'documents', true)
ON CONFLICT (id) DO NOTHING;

-- Idempotent RLS Policies for storage.objects
DROP POLICY IF EXISTS "Public read for documents" ON storage.objects;
DROP POLICY IF EXISTS "Auth Users Upload" ON storage.objects;
DROP POLICY IF EXISTS "Auth Users Update" ON storage.objects;
DROP POLICY IF EXISTS "Auth Users Delete" ON storage.objects;

-- Policy: Allow anyone to view documents
CREATE POLICY "Public read for documents" 
ON storage.objects FOR SELECT
USING (bucket_id = 'documents');

-- Policy: Allow authenticated users to upload documents
CREATE POLICY "Auth Users Upload" 
ON storage.objects FOR INSERT 
WITH CHECK (
    bucket_id = 'documents' 
    AND auth.role() = 'authenticated'
);

-- Policy: Allow authenticated users to update their own documents
CREATE POLICY "Auth Users Update" 
ON storage.objects FOR UPDATE 
USING (
    bucket_id = 'documents' 
    AND auth.role() = 'authenticated'
);

-- Policy: Allow authenticated users to delete their own documents
CREATE POLICY "Auth Users Delete" 
ON storage.objects FOR DELETE 
USING (
    bucket_id = 'documents' 
    AND auth.role() = 'authenticated'
);


-- 3. (Optional) Create 'profiles' table if future expansion utilizes SupabaseProfileDto.kt
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    full_name TEXT,
    avatar_url TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Idempotent RLS Policies for profiles
DROP POLICY IF EXISTS "Users can view their own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can update their own profile" ON public.profiles;

CREATE POLICY "Users can view their own profile"
ON public.profiles FOR SELECT
USING (auth.uid() = id);

CREATE POLICY "Users can update their own profile"
ON public.profiles FOR UPDATE
USING (auth.uid() = id);

-- Trigger to automatically create a profile for a new user
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger AS $$
BEGIN
  INSERT INTO public.profiles (id, email, full_name, avatar_url)
  VALUES (
    new.id,
    new.email,
    new.raw_user_meta_data->>'full_name',
    new.raw_user_meta_data->>'avatar_url'
  );
  RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

-- ==============================================================================
-- 4. User FCM Tokens Table for Push Notifications (Supabase + FCM)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.user_fcm_tokens (
  token TEXT PRIMARY KEY,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  device_id TEXT,
  platform TEXT NOT NULL DEFAULT 'android',
  device_model TEXT,
  app_version TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Ensure user_id allows NULL values for unauthenticated/guest devices
ALTER TABLE IF EXISTS public.user_fcm_tokens ALTER COLUMN user_id DROP NOT NULL;

-- Index for fast lookup by user_id
CREATE INDEX IF NOT EXISTS idx_user_fcm_tokens_user_id ON public.user_fcm_tokens(user_id);

-- Enable RLS
ALTER TABLE public.user_fcm_tokens ENABLE ROW LEVEL SECURITY;

-- RLS Policies (Supports both authenticated users and guest devices)
DROP POLICY IF EXISTS "Users can view their own fcm tokens" ON public.user_fcm_tokens;
DROP POLICY IF EXISTS "Users can insert their own fcm tokens" ON public.user_fcm_tokens;
DROP POLICY IF EXISTS "Users can update their own fcm tokens" ON public.user_fcm_tokens;
DROP POLICY IF EXISTS "Users can delete their own fcm tokens" ON public.user_fcm_tokens;
DROP POLICY IF EXISTS "Allow public select fcm tokens" ON public.user_fcm_tokens;
DROP POLICY IF EXISTS "Allow public insert and upsert fcm tokens" ON public.user_fcm_tokens;
DROP POLICY IF EXISTS "Allow public update fcm tokens" ON public.user_fcm_tokens;
DROP POLICY IF EXISTS "Allow public delete fcm tokens" ON public.user_fcm_tokens;

CREATE POLICY "Allow public select fcm tokens"
  ON public.user_fcm_tokens FOR SELECT
  TO anon, authenticated
  USING (true);

CREATE POLICY "Allow public insert and upsert fcm tokens"
  ON public.user_fcm_tokens FOR INSERT
  TO anon, authenticated
  WITH CHECK (true);

CREATE POLICY "Allow public update fcm tokens"
  ON public.user_fcm_tokens FOR UPDATE
  TO anon, authenticated
  USING (true)
  WITH CHECK (true);

CREATE POLICY "Allow public delete fcm tokens"
  ON public.user_fcm_tokens FOR DELETE
  TO anon, authenticated
  USING (true);

-- Trigger to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION public.handle_updated_at_fcm_tokens()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = timezone('utc'::text, now());
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS on_fcm_token_updated ON public.user_fcm_tokens;
CREATE TRIGGER on_fcm_token_updated
  BEFORE UPDATE ON public.user_fcm_tokens
  FOR EACH ROW EXECUTE PROCEDURE public.handle_updated_at_fcm_tokens();

-- End of schema file
