--liquibase formatted sql

--changeset cms-team:016-seed-staff-photos
--comment: Set team headshots (from brazentherapy.org/ourteam) on staff members that have none

UPDATE staff_members SET photo_url = '/images/team/alexis-welsh.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Alexis Welsh, LCSW' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/raven-taylor-aduwak.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Raven Taylor-Aduwak, LAC' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/ana-franco.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Ana Franco, LAC' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/aundrea-austin.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Aundrea Austin, LMSW' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/elise-pinkowski.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Elise Pinkowski, LMSW' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/makya-kirchner.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Makya Kirchner, LMSW' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/riana-burnett.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Riana Burnett, LCSW' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/alex-righi.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Alex Righi, LMSW' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/renz-narciso.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Renz Narciso, LMSW' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/cailin-payson.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Cailin Payson, LAMFT' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/erica-harris.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Erica Harris, LPC' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/vonyee-soulfire.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Vonyee Soulfire, LAC' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/teena-miller.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Teena Miller, LAMFT' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/chelsea-honea.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Chelsea Honea, LPC, LIAC' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/shae-moreau.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Shae Moreau, LAC' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/carissa-fenceroy.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Carissa Fenceroy, MSW' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/sybil-nwulu.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Sybil Nwulu, LAC' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/jeanine-whitehead.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Jeanine Whitehead, LCSW' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/maggie-reichler.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Maggie Reichler' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/sukhmani-khalsa.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Sukhmani Khalsa' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/kira-mcsherry.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Kira McSherry' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/robin-burnam.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Robin Burnam' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/mimi-jiang.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Mimi (Xiaojun) Jiang' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/angelinah-honea.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Angelinah Honea' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/stella-behnke.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Stella Behnke' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/amber-block-zambrano.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Amber Block-Zambrano, LCSW' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/shuheng-hu.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Shuheng Hu, LPC' AND (photo_url IS NULL OR photo_url = '');
UPDATE staff_members SET photo_url = '/images/team/clarke-scott.jpg' WHERE tenant_id = 'a0000000-0000-4000-8000-000000000001' AND name = 'Clarke Scott, LPC' AND (photo_url IS NULL OR photo_url = '');
