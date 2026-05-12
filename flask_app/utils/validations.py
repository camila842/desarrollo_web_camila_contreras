from datetime import datetime

VALID_ROLES       = {"Estudiante Pre Grado", "Estudiante Post Grado", "Funcionario", "Académico"}
SPECIAL_CHARS     = set("@¿?{}.")
VALID_FILE_TYPES  = {"png", "jpg", "jpeg", "mp4"}
VALID_TIPOS       = {"artistica", "deportiva", "tecnologica", "social", "recreativa", "otra"}


def validate_register_user(username, lastname, email, rol, password, comuna_id=None):
    if not username or len(username.strip()) < 3:
        return False, "El nombre debe tener al menos 3 caracteres."
    if not lastname or len(lastname.strip()) < 3:
        return False, "El apellido debe tener al menos 3 caracteres."
    if not rol or rol not in VALID_ROLES:
        return False, "Seleccione un rol válido."
    is_valid, err = _validate_email(email)
    if not is_valid:
        return False, err
    if not password or len(password) < 8:
        return False, "La contraseña debe tener al menos 8 caracteres."
    if not any(c in SPECIAL_CHARS for c in password):
        return False, "La contraseña debe tener al menos un carácter especial: @, ¿, ?, {, }, ."
    if not comuna_id:
        return False, "Seleccione una comuna."
    return True, ""


def validate_login_user(email, password):
    is_valid, err = _validate_email(email)
    if not is_valid:
        return False, err
    if not password:
        return False, "Ingrese su contraseña."
    return True, ""


def validate_activity(nombre, dias, hora_inicio, hora_fin, tipo, archivo=None, miembro_id=None):
    if not miembro_id:
        return False, "Debes iniciar sesión para registrar una actividad."
    if not nombre or len(nombre.strip()) < 3:
        return False, "El nombre debe tener al menos 3 caracteres."
    if not dias:
        return False, "Debes seleccionar al menos un día."
    if not hora_inicio or not hora_fin:
        return False, "Debe haber hora de inicio y término."
    is_valid, err = validate_time_activity(hora_inicio, hora_fin)
    if not is_valid:
        return False, err
    if not tipo or tipo not in VALID_TIPOS:
        return False, "Debes elegir el tipo de actividad."
    if archivo is not None:
        is_valid, err = _validate_file(archivo)
        if not is_valid:
            return False, err
    return True, ""


def validate_time_activity(t_i, t_f):
    try:
        init = datetime.strptime(t_i, "%H:%M")
        end  = datetime.strptime(t_f, "%H:%M")
    except ValueError:
        return False, "Formato de hora inválido."
    if end <= init:
        return False, "La hora de término debe ser mayor a la de inicio."
    return True, ""


def _validate_file(archivo):
    if not archivo or not archivo.filename:
        return False, "Debes ingresar al menos un archivo."
    ext = archivo.filename.rsplit(".", 1)[-1].lower()
    if ext not in VALID_FILE_TYPES:
        return False, "Solo se soportan archivos .png, .jpg, .jpeg, .mp4."
    return True, ""


def _validate_email(email):
    if not email:
        return False, "Email inválido."
    parts = email.split("@")
    if len(parts) != 2:
        return False, "Email inválido."
    name, domain = parts
    if len(name) < 3:
        return False, "Email inválido."
    if "." not in domain or not all(domain.split(".")):
        return False, "Email inválido."
    return True, ""
