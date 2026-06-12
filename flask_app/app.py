
import os
from database import db
from utils.helpers import *
from utils.validations import validate_register_user, validate_activity, validate_login_user
from flask import (
    Flask,
    request,
    render_template,
    redirect,
    url_for,
    session,
    flash,
    jsonify
)
from werkzeug.utils import secure_filename

app = Flask(__name__)

app.secret_key = "s3cr3t_k3y"

UPLOAD_FOLDER = os.path.join(app.root_path, "static", "uploads")
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

app.config["UPLOAD_FOLDER"] = UPLOAD_FOLDER

# --- Main Routes ---
@app.route("/")
def index():
    last_members = db.get_last_members()
    return render_template("interfaces/welcome.html", members=last_members)


# --- Auth Routes ---
@app.route("/register", methods=["GET", "POST"])
def register():
    if request.method == "GET":
        comunas = db.get_comunas()
        return render_template("interfaces/registro.html", comunas=comunas)

    # POST
    username    = request.form.get("name", "").strip()
    user_lastname = request.form.get("surname", "").strip()
    rol         = request.form.get("role", "")
    email       = request.form.get("email", "").strip()
    password    = request.form.get("password", "")
    comuna_id   = request.form.get("comuna_id") 

    is_valid, error = validate_register_user(username, user_lastname, email, rol, password, comuna_id)
    if is_valid:
        status, msg = db.register_user(username, user_lastname, email, rol, password, comuna_id)
        if status:
            session["user"]       = username
            session["miembro_id"] = msg
            return redirect(url_for("index"))
        error = msg

    flash(error)
    return redirect(url_for("register"))


@app.route("/activity", methods=["GET", "POST"])
def register_activity():
    miembro_id = session.get("miembro_id")

    if miembro_id is None:
        flash("Debes iniciar sesión para registrar una actividad.")
        return redirect(url_for("login"))

    if request.method == "GET":
        return render_template("interfaces/actividades.html")

    dias = request.form.getlist("week-day")
    # hora_inicio = request.form.get("from-time", "")
    # hora_fin = request.form.get("until-time", "")
    hora_inicio = request.form.getlist("from-time")[-1]
    hora_fin = request.form.getlist("until-time")[-1]
    tipo = request.form.get("tipo", "")
    nombre_actividad = request.form.get("activity-name", "").strip()
    archivo = request.files.get("file-input")
    miembro_id = session.get("miembro_id")

    print("FORM:", request.form)
    print("FILES:", request.files)
    print("miembro_id:", miembro_id)
    print("dias:", dias)
    print("hora_inicio:", hora_inicio)
    print("hora_fin:", hora_fin)
    print("tipo:", tipo)
    print("nombre_actividad:", nombre_actividad)

    is_valid, error = validate_activity(
        nombre_actividad, dias, hora_inicio, hora_fin, tipo, archivo, miembro_id
    )

    print("is_valid:", is_valid)
    print("validation_error:", error)

    if is_valid:
        status, result = db.register_activity(
            nombre_actividad, dias, hora_inicio, hora_fin, tipo, miembro_id
        )

        print("db_status:", status)
        print("db_result:", result)

        if status:
            if archivo and archivo.filename:
                filename = secure_filename(archivo.filename)
                ruta_archivo = os.path.join(UPLOAD_FOLDER, filename)
                archivo.save(ruta_archivo)

                ruta_db = f"uploads/{filename}"
                db.register_foto(result, ruta_archivo, ruta_db)

            return redirect(url_for("register_activity"))

        error = result

    flash(error)
    return redirect(url_for("register_activity"))


@app.route("/login", methods=["GET", "POST"])
def login():
    print("\n--- /login ---")
    print("method:", request.method)
    print("session antes:", dict(session))

    if request.method == "POST":
        email = request.form.get("email", "").strip()
        password = request.form.get("contrasenna", "")

        print("FORM:", request.form)
        print("email:", email)
        print("password vacío?:", password == "")
        print("password length:", len(password))

        is_valid, error = validate_login_user(email, password)

        print("is_valid:", is_valid)
        print("validation_error:", error)

        if is_valid:
            status, result = db.login_user(email, password)

            print("db_status:", status)
            print("db_result:", result)
            print("type(result):", type(result))

            if status:
                print("result.email:", getattr(result, "email", None))
                print("result.id:", getattr(result, "id", None))

                session["user"] = result.email
                session["miembro_id"] = result.id

                print("session después:", dict(session))
                print("redirigiendo a index")

                return redirect(url_for("index"))

            error = result

        print("login falló, error:", error)
        return render_template("interfaces/login.html", error=error)

    elif request.method == "GET":
        print("GET /login")
        print("session user:", session.get("user"))
        print("session miembro_id:", session.get("miembro_id"))

        if session.get("user", None):
            print("ya hay user en session, redirigiendo a index")
            return redirect(url_for("index"))
        else:
            print("sin session, mostrando login")
            return render_template("interfaces/login.html")
        
@app.route("/logout")
def logout():
    session.clear()
    return redirect(url_for("login"))

@app.route("/members", methods=["GET", "POST"])
def members():
    if request.method == "GET":
        pagina = request.args.get("page", 1, type=int)
        por_pagina = 5
        offset = (pagina - 1) * por_pagina

        tipo = request.args.get("tipo", "")
        orden_attr = request.args.get("orden_attr", "")
        orden_dir = request.args.get("orden_dir", "")

        miembros = db.get_miembros_paginados(
            limit=por_pagina,
            offset=offset,
            tipo=tipo,
            orden_attr=orden_attr,
            orden_dir=orden_dir
        )

        total = db.count_miembros(tipo=tipo)

        return render_template(
            "interfaces/miembros.html",
            miembros=miembros,
            pagina=pagina,
            hay_anterior=pagina > 1,
            hay_siguiente=offset + por_pagina < total,
            tipo=tipo,
            orden_attr=orden_attr,
            orden_dir=orden_dir
        )
    
@app.route("/members/<int:member_id>", methods=["GET"])
def member_detail(member_id):
    tipo = request.args.get("tipo", "")
    orden_attr = request.args.get("orden_attr", "")
    orden_dir = request.args.get("orden_dir", "")

    miembro = db.get_miembro_by_id(member_id)

    actividades = db.get_actividades_by_miembro_id(
        member_id=member_id,
        tipo=tipo,
        orden_attr=orden_attr,
        orden_dir=orden_dir
    )

    return render_template(
        "interfaces/detalle_miembro.html",
        miembro=miembro,
        actividades=actividades,
        tipo=tipo,
        orden_attr=orden_attr,
        orden_dir=orden_dir
    )
    
@app.route("/api/miembros-por-dia")
def miembros_por_dia():
    data = db.get_miembros_registrados_por_dia()
    return jsonify(data)

@app.route("/metricas")
def metricas():
    return render_template("interfaces/metricas.html")

@app.route("/api/actividades-por-tipo")
def api_actividades_por_tipo():
    data = db.get_total_actividades_por_tipo()
    return jsonify(data)

@app.route("/api/actividades-por-comuna")
def api_actividades_por_comuna():
    data = db.get_total_actividades_por_comuna()
    return jsonify(data)

@app.route("/activities/<int:actividad_id>", methods=["GET"])
def activity_detail(actividad_id):
    actividad = db.get_actividad_by_id(actividad_id)

    if actividad is None:
        return "Actividad no encontrada", 404

    comentarios = db.get_comentarios_by_actividad_id(actividad_id)

    return render_template(
        "interfaces/detalle_actividad.html",
        actividad=actividad,
        comentarios=comentarios,
        errores=[],
        nombre_anterior="",
        texto_anterior=""
    )

@app.route("/activities/<int:actividad_id>/comentarios", methods=["POST"])
def agregar_comentario(actividad_id):
    actividad = db.get_actividad_by_id(actividad_id)

    if actividad is None:
        return "Actividad no encontrada", 404

    nombre = request.form.get("nombre", "").strip()
    texto = request.form.get("texto", "").strip()

    errores = []

    if len(nombre) < 3 or len(nombre) > 80:
        errores.append("El nombre debe tener entre 3 y 80 caracteres.")

    if len(texto) < 5:
        errores.append("El comentario debe tener al menos 5 caracteres.")

    if errores:
        comentarios = db.get_comentarios_by_actividad_id(actividad_id)

        return render_template(
            "interfaces/detalle_actividad.html",
            actividad=actividad,
            comentarios=comentarios,
            errores=errores,
            nombre_anterior=nombre,
            texto_anterior=texto
        )

    db.crear_comentario(
        nombre=nombre,
        texto=texto,
        actividad_id=actividad_id
    )

    return redirect(url_for("activity_detail", actividad_id=actividad_id))

@app.route("/api/activities/<int:actividad_id>/comentarios", methods=["GET"])
def api_get_comentarios(actividad_id):
    actividad = db.get_actividad_by_id(actividad_id)

    if actividad is None:
        return jsonify({"ok": False, "errores": ["Actividad no encontrada"]}), 404

    comentarios = db.get_comentarios_by_actividad_id(actividad_id)

    data = [
        {
            "fecha": comentario.fecha.strftime("%Y-%m-%d %H:%M:%S"),
            "nombre": comentario.nombre,
            "texto": comentario.texto
        }
        for comentario in comentarios
    ]

    return jsonify({
        "ok": True,
        "comentarios": data
    })
    
@app.route("/api/activities/<int:actividad_id>/comentarios", methods=["POST"])
def api_agregar_comentario(actividad_id):
    actividad = db.get_actividad_by_id(actividad_id)

    if actividad is None:
        return jsonify({"ok": False, "errores": ["Actividad no encontrada"]}), 404

    data = request.get_json()

    nombre = data.get("nombre", "").strip()
    texto = data.get("texto", "").strip()

    errores = []

    if len(nombre) < 3 or len(nombre) > 80:
        errores.append("El nombre debe tener entre 3 y 80 caracteres.")

    if len(texto) < 5 or len(texto) > 300:
        errores.append("El comentario debe tener entre 5 y 300 caracteres.")

    if errores:
        return jsonify({
            "ok": False,
            "errores": errores
        }), 400

    db.crear_comentario(
        nombre=nombre,
        texto=texto,
        actividad_id=actividad_id
    )

    return jsonify({
        "ok": True,
        "mensaje": "Comentario agregado correctamente."
    })