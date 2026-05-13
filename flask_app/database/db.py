from collections import defaultdict
from sqlalchemy import create_engine, Column, Integer, String, DateTime, Text, Enum, ForeignKey
from sqlalchemy.orm import sessionmaker, declarative_base, relationship
from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash

DB_NAME = "tarea2"
DB_USERNAME = "db_admin"
DB_PASSWORD = "dbadmin"
DB_HOST = "localhost"
DB_PORT = 3306

DATABASE_URL = f"mysql+pymysql://{DB_USERNAME}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}"

engine = create_engine(DATABASE_URL, echo=False, future=True)
SessionLocal = sessionmaker(bind=engine)

Base = declarative_base()


# --- Models ---

class Region(Base):
    __tablename__ = "region"

    id     = Column(Integer, primary_key=True)
    nombre = Column(String(200), nullable=False)

    comunas = relationship("Comuna", back_populates="region")


class Comuna(Base):
    __tablename__ = "comuna"

    id        = Column(Integer, primary_key=True)
    nombre    = Column(String(200), nullable=False)
    region_id = Column(Integer, ForeignKey("region.id"), nullable=False)

    region = relationship("Region", back_populates="comunas")


class Miembro(Base):
    __tablename__ = "miembro"

    id             = Column(Integer, primary_key=True, autoincrement=True)
    nombre         = Column(String(255), nullable=False)
    apellido       = Column(String(255), nullable=True)
    rol            = Column(String(50), nullable=True)
    email          = Column(String(80), nullable=False)
    password_hash  = Column(String(255), nullable=True)
    fecha_registro = Column(DateTime, nullable=False)
    comuna_id      = Column(Integer, ForeignKey("comuna.id"), nullable=True)

    actividades = relationship("Actividad", back_populates="miembro")


class Foto(Base):
    __tablename__ = "foto"

    id             = Column(Integer, primary_key=True, autoincrement=True)
    ruta_archivo   = Column(String(300), nullable=False)
    nombre_archivo = Column(String(300), nullable=False)
    actividad_id   = Column(Integer, ForeignKey("actividad.id"), nullable=False)


class Actividad(Base):
    __tablename__ = "actividad"

    id          = Column(Integer, primary_key=True, autoincrement=True)
    miembro_id  = Column(Integer, ForeignKey("miembro.id"), nullable=False)
    dia         = Column(Enum("lunes","martes","miércoles","jueves","viernes","sábado","domingo"), nullable=False)
    hora_inicio = Column(String(5), nullable=False)
    duracion    = Column(String(5), nullable=False)
    tipo        = Column(Enum("artistica","deportiva","tecnologica","social","recreativa","otra"), nullable=False)
    nombre      = Column(String(45), nullable=False)
    descripcion = Column(Text, nullable=True)

    miembro = relationship("Miembro", back_populates="actividades")


# --- Database Functions ---

def register_user(username, lastname, email, rol, password, comuna_id):
    session = SessionLocal()
    try:
        miembro = Miembro(
            nombre=username,
            apellido=lastname,
            email=email,
            rol=rol,
            password_hash=generate_password_hash(password),
            fecha_registro=datetime.now(),
            comuna_id=comuna_id if comuna_id else None,
        )
        session.add(miembro)
        session.flush()
        miembro_id = miembro.id
        session.commit()
        return 1, miembro_id
    except Exception as e:
        session.rollback()
        return 0, str(e)
    finally:
        session.close()


def register_foto(actividad_id, ruta_archivo, nombre_archivo):
    session = SessionLocal()
    try:
        foto = Foto(
            actividad_id=actividad_id,
            ruta_archivo=ruta_archivo,
            nombre_archivo=nombre_archivo,
        )
        session.add(foto)
        session.commit()
        return 1, ""
    except Exception as e:
        session.rollback()
        return 0, str(e)
    finally:
        session.close()


def _calcular_duracion(hora_inicio, hora_fin):
    init = datetime.strptime(hora_inicio, "%H:%M")
    end  = datetime.strptime(hora_fin,    "%H:%M")
    total_min = int((end - init).total_seconds() / 60)
    return f"{total_min // 60:02d}:{total_min % 60:02d}"


def register_activity(nombre, dias, hora_inicio, hora_fin, tipo, miembro_id):
    session = SessionLocal()
    try:
        duracion = _calcular_duracion(hora_inicio, hora_fin)
        last_id  = None
        for dia in dias:
            actividad = Actividad(
                miembro_id=miembro_id,
                dia=dia,
                hora_inicio=hora_inicio,
                duracion=duracion,
                tipo=tipo,
                nombre=nombre,
            )
            session.add(actividad)
            session.flush()
            last_id = actividad.id
        session.commit()
        return 1, last_id
    except Exception as e:
        session.rollback()
        return 0, str(e)
    finally:
        session.close()


def get_comunas():
    session = SessionLocal()
    try:
        rows = (
            session.query(Comuna, Region)
            .join(Region, Region.id == Comuna.region_id)
            .order_by(Region.id, Comuna.nombre)
            .all()
        )
        comunas_by_region = defaultdict(list)
        region_names = {}
        for comuna, region in rows:
            comunas_by_region[region.id].append({"id": comuna.id, "nombre": comuna.nombre})
            region_names[region.id] = region.nombre
        return [
            {"id": rid, "nombre": region_names[rid], "comunas": comunas_by_region[rid]}
            for rid in sorted(comunas_by_region.keys())
        ]
    except Exception:
        return []
    finally:
        session.close()


def get_last_members(limit=5):
    session = SessionLocal()
    try:
        rows = (
            session.query(
                Miembro.nombre,
                Miembro.email,
                Actividad.nombre.label("actividad"),
                Actividad.tipo,
            )
            .outerjoin(Actividad, Actividad.miembro_id == Miembro.id)
            .order_by(Miembro.fecha_registro.desc())
            .limit(limit)
            .all()
        )
        return [row._asdict() for row in rows]
    except Exception:
        return []
    finally:
        session.close()


def get_user_by_email(email):
    session = SessionLocal()
    user = session.query(Miembro).filter(Miembro.email == email).first()
    session.close()
    return user


def login_user(email, password):
    a_user = get_user_by_email(email)
    if a_user is None:
        return False, "Usuario o contraseña incorrectos."
    if not check_password_hash(a_user.password_hash, password):
        return False, "Usuario o contraseña incorrectos."
    return True, a_user

def get_miembros_paginados(limit, offset):
    session = SessionLocal()

    miembros = (
        session.query(Miembro)
        .order_by(Miembro.id.asc())
        .limit(limit)
        .offset(offset)
        .all()
    )

    # resultado = []

    # for miembro in miembros:
    #     resultado.append({
    #         "nombre": miembro.nombre,
    #         "apellido":miembro.apellido,
    #         "rol":miembro.rol,
    #         "email": miembro.email,
    #         "region": miembro.comuna_id            
    #     })

    session.close()
    # return resultado
    return miembros


def count_miembros():
    session = SessionLocal()

    total = session.query(Miembro).count()

    session.close()
    return total