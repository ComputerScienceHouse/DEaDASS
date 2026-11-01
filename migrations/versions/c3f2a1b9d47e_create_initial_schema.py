"""Create initial database and s3 tables

Revision ID: c3f2a1b9d47e
Revises:
Create Date: 2026-10-31

"""
from alembic import op

# revision identifiers, used by Alembic.
revision = "c3f2a1b9d47e"
down_revision = None
branch_labels = None
depends_on = None


def upgrade():
    op.execute(
        """
        CREATE TABLE public.database (
            id integer NOT NULL,
            name text NOT NULL,
            owner text NOT NULL,
            db_type text,
            CONSTRAINT database_pkey PRIMARY KEY (id)
        )
        """
    )

    op.execute(
        """
        ALTER TABLE public.database ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
            SEQUENCE NAME public.database_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1
        )
        """
    )

    op.execute(
        """
        CREATE TABLE public.s3 (
            id integer NOT NULL,
            name character varying NOT NULL,
            access_key character varying NOT NULL,
            owner character varying NOT NULL,
            CONSTRAINT s3_pkey PRIMARY KEY (id)
        )
        """
    )

    op.execute(
        """
        ALTER TABLE public.s3 ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
            SEQUENCE NAME public.s3_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1
        )
        """
    )


def downgrade():
    op.execute("DROP TABLE IF EXISTS public.s3")
    op.execute("DROP TABLE IF EXISTS public.database")