package com.tarea4.tarea4.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tarea4.tarea4.models.Actividad;
import com.tarea4.tarea4.models.Comentario;
import com.tarea4.tarea4.models.Miembro;
import com.tarea4.tarea4.services.AppService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AppController {

    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Map<String, String>> lastMembers = appService.getLastMembers(5);
        model.addAttribute("members", lastMembers);
        return "welcome";
    }

    @GetMapping("/register")
    public String registerGet(Model model) {
        List<Map<String, Object>> comunas = appService.getComunas();
        model.addAttribute("comunas", comunas);
        return "registro";
    }

    @PostMapping("/register")
    public String registerPost(
        @RequestParam("name") String username,
        @RequestParam("surname") String userLastname,
        @RequestParam("role") String rol,
        @RequestParam("email") String email,
        @RequestParam("password") String password,
        @RequestParam("comuna_id") Integer comunaId,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Miembro miembro = appService.registerUser(
                username.trim(),
                userLastname.trim(),
                email.trim(),
                rol,
                password,
                comunaId
            );

            session.setAttribute("user", miembro.getNombre());
            session.setAttribute("miembro_id", miembro.getId());

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/activity")
    public String activityGet(
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        Integer miembroId = (Integer) session.getAttribute("miembro_id");

        if (miembroId == null) {
            redirectAttributes.addFlashAttribute(
                "error",
                "Debes iniciar sesión para registrar una actividad."
            );
            return "redirect:/login";
        }

        return "actividades";
    }

    @PostMapping("/activity")
    public String activityPost(
        @RequestParam("week-day") List<String> dias,
        @RequestParam("from-time") List<String> fromTimes,
        @RequestParam("until-time") List<String> untilTimes,
        @RequestParam("tipo") String tipo,
        @RequestParam("activity-name") String nombreActividad,
        @RequestParam(value = "file-input", required = false) MultipartFile archivo,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        Integer miembroId = (Integer) session.getAttribute("miembro_id");

        if (miembroId == null) {
            redirectAttributes.addFlashAttribute(
                "error",
                "Debes iniciar sesión para registrar una actividad."
            );
            return "redirect:/login";
        }

        try {
            String horaInicio = fromTimes.get(fromTimes.size() - 1);
            String horaFin = untilTimes.get(untilTimes.size() - 1);

            Integer actividadId = appService.registerActivity(
                nombreActividad.trim(),
                dias,
                horaInicio,
                horaFin,
                tipo,
                miembroId,
                archivo
            );

            if (archivo != null && !archivo.isEmpty()) {
                appService.registerFotoFromUpload(actividadId, archivo);
            }

            return "redirect:/activity";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/activity";
        }
    }

    @GetMapping("/login")
    public String loginGet(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }

        return "login";
    }

    @PostMapping("/login")
    public String loginPost(
        @RequestParam("email") String email,
        @RequestParam("contrasenna") String password,
        HttpSession session,
        Model model
    ) {
        try {
            Miembro miembro = appService.loginUser(email.trim(), password);

            if (miembro == null) {
                model.addAttribute("error", "Usuario o contraseña incorrectos.");
                return "login";
            }

            session.setAttribute("user", miembro.getEmail());
            session.setAttribute("miembro_id", miembro.getId());

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/members")
    public String members(
        @RequestParam(value = "page", defaultValue = "1") Integer pagina,
        @RequestParam(value = "tipo", defaultValue = "") String tipo,
        @RequestParam(value = "orden_attr", defaultValue = "") String ordenAttr,
        @RequestParam(value = "orden_dir", defaultValue = "") String ordenDir,
        Model model
    ) {
        Integer porPagina = 5;
        Integer offset = (pagina - 1) * porPagina;

        List<Miembro> miembros = appService.getMiembrosPaginados(
            porPagina,
            offset,
            tipo,
            ordenAttr,
            ordenDir
        );

        Long total = appService.countMiembros(tipo);

        model.addAttribute("miembros", miembros);
        model.addAttribute("pagina", pagina);
        model.addAttribute("hay_anterior", pagina > 1);
        model.addAttribute("hay_siguiente", offset + porPagina < total);
        model.addAttribute("tipo", tipo);
        model.addAttribute("orden_attr", ordenAttr);
        model.addAttribute("orden_dir", ordenDir);

        return "miembros";
    }

    @GetMapping("/members/{member_id}")
    public String memberDetail(
        @PathVariable("member_id") Integer memberId,
        @RequestParam(value = "tipo", defaultValue = "") String tipo,
        @RequestParam(value = "orden_attr", defaultValue = "") String ordenAttr,
        @RequestParam(value = "orden_dir", defaultValue = "") String ordenDir,
        Model model
    ) {
        Miembro miembro = appService.getMiembroById(memberId);

        List<Actividad> actividades = appService.getActividadesByMiembroId(
            memberId,
            tipo,
            ordenAttr,
            ordenDir
        );

        model.addAttribute("miembro", miembro);
        model.addAttribute("actividades", actividades);
        model.addAttribute("tipo", tipo);
        model.addAttribute("orden_attr", ordenAttr);
        model.addAttribute("orden_dir", ordenDir);

        return "detalle_miembro";
    }

    @GetMapping("/metricas")
    public String metricas() {
        return "metricas";
    }

    @GetMapping("/activities/{actividad_id}")
    public String activityDetail(
        @PathVariable("actividad_id") Integer actividadId,
        Model model
    ) {
        Actividad actividad = appService.getActividadById(actividadId);

        if (actividad == null) {
            return "error/404";
        }

        List<Comentario> comentarios = appService.getComentariosByActividadId(actividadId);

        model.addAttribute("actividad", actividad);
        model.addAttribute("comentarios", comentarios);
        model.addAttribute("errores", List.of());
        model.addAttribute("nombre_anterior", "");
        model.addAttribute("texto_anterior", "");

        return "detalle_actividad";
    }

    @PostMapping("/activities/{actividad_id}/comentarios")
    public String agregarComentario(
        @PathVariable("actividad_id") Integer actividadId,
        @RequestParam("nombre") String nombre,
        @RequestParam("texto") String texto,
        Model model
    ) {
        Actividad actividad = appService.getActividadById(actividadId);

        if (actividad == null) {
            return "error/404";
        }

        try {
            String palabrasProhibidasPath = "src/main/resources/data/palabras_prohibidas.txt";

            appService.crearComentario(
                nombre.trim(),
                texto.trim(),
                actividadId,
                palabrasProhibidasPath
            );

            return "redirect:/activities/" + actividadId;
        } catch (IllegalArgumentException e) {
            List<Comentario> comentarios = appService.getComentariosByActividadId(actividadId);

            model.addAttribute("actividad", actividad);
            model.addAttribute("comentarios", comentarios);
            model.addAttribute("errores", List.of(e.getMessage()));
            model.addAttribute("nombre_anterior", nombre);
            model.addAttribute("texto_anterior", texto);

            return "detalle_actividad";
        }
    }
}