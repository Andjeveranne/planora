package com.isi4.userservice.config;

import com.isi4.userservice.model.Administrateur;
import com.isi4.userservice.model.Classe;
import com.isi4.userservice.model.Course;
import com.isi4.userservice.model.Disponibilite;
import com.isi4.userservice.model.Enseignant;
import com.isi4.userservice.model.UE;
import com.isi4.userservice.model.Utilisateur;
import com.isi4.userservice.repository.AdministrateurRepository;
import com.isi4.userservice.repository.ClasseRepository;
import com.isi4.userservice.repository.CourseRepository;
import com.isi4.userservice.repository.DisponibiliteRepository;
import com.isi4.userservice.repository.EnseignantRepository;
import com.isi4.userservice.repository.UERepository;
import com.isi4.userservice.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdministrateurRepository administrateurRepository;
    private final EnseignantRepository enseignantRepository;
    private final ClasseRepository classeRepository;
    private final UERepository ueRepository;
    private final DisponibiliteRepository disponibiliteRepository;
    private final CourseRepository courseRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedAcademicData();
    }

    private void seedAdmin() {
        if (utilisateurRepository.findById("admin001").isPresent()
                || utilisateurRepository.existsByEmail("admin.systeme@ictuniversity.org")) {
            return;
        }

        Administrateur admin = new Administrateur();
        admin.setIdUser("admin001");
        admin.setMdp(passwordEncoder.encode("password123"));
        admin.setEmail("admin.systeme@ictuniversity.org");
        admin.setNomUser("Systeme");
        admin.setPrenomUser("Admin");
        admin.setTelephone("0000000000");
        admin.setRole("ADMIN");
        administrateurRepository.save(admin);
        System.out.println("✅ Administrateur créé : admin.systeme@ictuniversity.org / password123");
    }

    private void seedAcademicData() {
        boolean alreadySeeded = enseignantRepository.count() > 0 || ueRepository.count() > 0 || classeRepository.count() > 0 || courseRepository.count() > 0;
        if (alreadySeeded) {
            System.out.println("ℹ️ Données académiques déjà initialisées. Aucune réécriture effectuée au démarrage.");
            return;
        }

        List<TeacherSeed> teachers = List.of(
                new TeacherSeed("ens001", "nkiamboh.tanwi@ictuniversity.org", "Tanwi", "Nkiamboh", "Informatics", "ICT_UNIVERSITY", "Software Engineering"),
                new TeacherSeed("ens002", "marie.mba@ictuniversity.org", "Mba", "Marie", "Informatics", "ICT_UNIVERSITY", "Data Science"),
                new TeacherSeed("ens003", "alain.dibongo@ictuniversity.org", "Dibongo", "Alain", "Networking & Security", "ICT_UNIVERSITY", "Cybersecurity"),
                new TeacherSeed("ens004", "grace.ekeme@ictuniversity.org", "Ekeme", "Grace", "Business & Finance", "ICT_UNIVERSITY", "Finance"),
                new TeacherSeed("ens005", "pierre.ndjock@ictuniversity.org", "Ndjock", "Pierre", "Business & Finance", "ICT_UNIVERSITY", "Digital Marketing"),
                new TeacherSeed("ens006", "sophie.mokam@ictuniversity.org", "Mokam", "Sophie", "Civil Engineering", "ICT_UNIVERSITY", "Structures"),
                new TeacherSeed("ens007", "jean.mbem@ictuniversity.org", "Mbem", "Jean", "Informatics", "ICT_UNIVERSITY", "Cloud & DevOps"),
                new TeacherSeed("ens008", "louise.tchinda@ictuniversity.org", "Tchinda", "Louise", "Networking & Security", "ICT_UNIVERSITY", "Network Systems"),
                new TeacherSeed("ens009", "daniel.nguefack@ictuniversity.org", "Nguefack", "Daniel", "Business & Finance", "ICT_UNIVERSITY", "Human Resource Management"),
                new TeacherSeed("ens010", "charles.ekoume@ictuniversity.org", "Ekoume", "Charles", "Civil Engineering", "ICT_UNIVERSITY", "Construction Management"),
                new TeacherSeed("ens011", "john.martin@ictuniversity.org", "Martin", "John", "Engineering", "ICT_UNIVERSITY", "Software Engineering")
        );

        List<ClassSeed> classSeeds = List.of(
                new ClassSeed("L1 INFO A", "L1", "Informatics", 48),
                new ClassSeed("L2 INFO A", "L2", "Informatics", 46),
                new ClassSeed("L3 INFO A", "L3", "Informatics", 42),
                new ClassSeed("L1 RES A", "L1", "Networking & Security", 40),
                new ClassSeed("L2 RES A", "L2", "Networking & Security", 38),
                new ClassSeed("L1 BIZ A", "L1", "Business & Finance", 52),
                new ClassSeed("L2 BIZ A", "L2", "Business & Finance", 50),
                new ClassSeed("L3 BIZ A", "L3", "Business & Finance", 44),
                new ClassSeed("L1 CIV A", "L1", "Civil Engineering", 36),
                new ClassSeed("L3 CIV A", "L3", "Civil Engineering", 34),
                new ClassSeed("L1 ENG A", "L1", "Engineering", 45),
                new ClassSeed("L2 ENG A", "L2", "Engineering", 40),
                new ClassSeed("L3 ENG A", "L3", "Engineering", 38)
        );

        List<UESeed> ueSeeds = List.of(
                new UESeed("INF101", "Algorithmics", "S1", "L1", "Informatics", 6, 2, 2, 2, "Foundations of algorithms and computational thinking"),
                new UESeed("INF102", "Data Structures", "S1", "L1", "Informatics", 6, 2, 2, 2, "Abstract data types and memory organization"),
                new UESeed("INF201", "Object-Oriented Programming", "S2", "L2", "Informatics", 6, 2, 2, 2, "Design and implementation of object-oriented software"),
                new UESeed("INF202", "Database Systems", "S2", "L2", "Informatics", 6, 2, 2, 2, "Relational modeling and SQL fundamentals"),
                new UESeed("INF301", "Artificial Intelligence", "S3", "L3", "Informatics", 6, 2, 2, 2, "Machine learning and applied AI methods"),
                new UESeed("INF302", "Cloud Computing", "S3", "L3", "Informatics", 6, 2, 2, 2, "Cloud architectures and container orchestration"),
                new UESeed("RES101", "Network Fundamentals", "S1", "L1", "Networking & Security", 5, 2, 1, 2, "Protocols, addressing and topology"),
                new UESeed("RES201", "Network Administration", "S2", "L2", "Networking & Security", 6, 2, 2, 2, "Configuration, troubleshooting and security hardening"),
                new UESeed("RES301", "System Security", "S3", "L3", "Networking & Security", 6, 2, 2, 2, "Cybersecurity and vulnerability analysis"),
                new UESeed("BIZ101", "Financial Accounting", "S1", "L1", "Business & Finance", 5, 2, 1, 2, "Principles of accounting and reporting"),
                new UESeed("BIZ201", "Financial Management", "S2", "L2", "Business & Finance", 5, 2, 1, 2, "Investment and corporate finance analysis"),
                new UESeed("BIZ301", "Strategic Marketing", "S3", "L3", "Business & Finance", 5, 2, 1, 2, "Marketing strategy and digital engagement"),
                new UESeed("CIV101", "Geotechnics", "S1", "L1", "Civil Engineering", 5, 2, 1, 2, "Soil mechanics and foundations"),
                new UESeed("CIV201", "Structural Analysis", "S2", "L2", "Civil Engineering", 6, 2, 2, 2, "Design and assessment of structural systems"),
                new UESeed("CIV301", "Sustainable Construction", "S3", "L3", "Civil Engineering", 6, 2, 2, 2, "Worksite quality management and sustainability"),
                new UESeed("ENG101", "Software Engineering Fundamentals", "S1", "L1", "Engineering", 6, 2, 2, 2, "Requirements, design and software lifecycle"),
                new UESeed("ENG201", "Software Design and Architecture", "S2", "L2", "Engineering", 6, 2, 2, 2, "Patterns, architecture and maintainability"),
                new UESeed("ENG301", "Advanced Software Engineering", "S3", "L3", "Engineering", 6, 2, 2, 2, "Agile engineering, testing and delivery")
        );

        Map<String, Enseignant> savedTeachers = new LinkedHashMap<>();
        for (TeacherSeed teacherSeed : teachers) {
            Enseignant teacher = createTeacher(
                    teacherSeed.id(), teacherSeed.email(), teacherSeed.nom(), teacherSeed.prenom(),
                    teacherSeed.departement(), teacherSeed.school(), teacherSeed.specialite());
            if (teacher != null) {
                savedTeachers.put(teacherSeed.email(), teacher);
            }
        }

        List<Classe> savedClasses = new ArrayList<>();
        for (ClassSeed classSeed : classSeeds) {
            if (!classeRepository.existsByNomClasse(classSeed.nom())) {
                Classe classe = createClass(classSeed.nom(), classSeed.niveau(), classSeed.filiere(), "2025-2026", classSeed.effectif());
                savedClasses.add(classeRepository.save(classe));
            }
        }

        List<UE> savedUes = new ArrayList<>();
        for (UESeed ueSeed : ueSeeds) {
            if (!ueRepository.existsByCodeUE(ueSeed.code())) {
                UE ue = createUE(ueSeed.code(), ueSeed.nom(), ueSeed.description(), ueSeed.cm(), ueSeed.td(), ueSeed.tp(), ueSeed.credits(), ueSeed.semestre(), ueSeed.niveau());
                savedUes.add(ueRepository.save(ue));
            }
        }

        if (!savedTeachers.isEmpty()) {
            assignTeacherToUE(savedTeachers.get("nkiamboh.tanwi@ictuniversity.org"), ueRepository.findAll().stream().filter(ue -> ue.getCodeUE().startsWith("INF")).toArray(UE[]::new));
            assignTeacherToUE(savedTeachers.get("marie.mba@ictuniversity.org"), ueRepository.findAll().stream().filter(ue -> ue.getCodeUE().startsWith("INF") && ue.getNiveau().equals("L2")).toArray(UE[]::new));
            assignTeacherToUE(savedTeachers.get("alain.dibongo@ictuniversity.org"), ueRepository.findAll().stream().filter(ue -> ue.getCodeUE().startsWith("RES")).toArray(UE[]::new));
            assignTeacherToUE(savedTeachers.get("grace.ekeme@ictuniversity.org"), ueRepository.findAll().stream().filter(ue -> ue.getCodeUE().startsWith("BIZ")).toArray(UE[]::new));
            assignTeacherToUE(savedTeachers.get("pierre.ndjock@ictuniversity.org"), ueRepository.findAll().stream().filter(ue -> ue.getCodeUE().startsWith("BIZ") && ue.getNiveau().equals("L3")).toArray(UE[]::new));
            assignTeacherToUE(savedTeachers.get("sophie.mokam@ictuniversity.org"), ueRepository.findAll().stream().filter(ue -> ue.getCodeUE().startsWith("CIV")).toArray(UE[]::new));
            assignTeacherToUE(savedTeachers.get("jean.mbem@ictuniversity.org"), ueRepository.findAll().stream().filter(ue -> ue.getCodeUE().startsWith("INF") && ue.getNiveau().equals("L3")).toArray(UE[]::new));
            assignTeacherToUE(savedTeachers.get("louise.tchinda@ictuniversity.org"), ueRepository.findAll().stream().filter(ue -> ue.getCodeUE().startsWith("RES") && ue.getNiveau().equals("L1")).toArray(UE[]::new));
            assignTeacherToUE(savedTeachers.get("daniel.nguefack@ictuniversity.org"), ueRepository.findAll().stream().filter(ue -> ue.getCodeUE().startsWith("BIZ") && ue.getNiveau().equals("L1")).toArray(UE[]::new));
            assignTeacherToUE(savedTeachers.get("charles.ekoume@ictuniversity.org"), ueRepository.findAll().stream().filter(ue -> ue.getCodeUE().startsWith("CIV") && ue.getNiveau().equals("L1")).toArray(UE[]::new));
            assignTeacherToUE(savedTeachers.get("john.martin@ictuniversity.org"), ueRepository.findAll().stream().filter(ue -> ue.getCodeUE().startsWith("ENG")).toArray(UE[]::new));
        }

        for (Map.Entry<String, Enseignant> entry : savedTeachers.entrySet()) {
            seedAvailability(entry.getValue(), DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0));
            seedAvailability(entry.getValue(), DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(12, 0));
        }

        // No automatic default availability is seeded anymore. Teachers must declare their
        // own weekly availability through the user-service API so the admin view matches the
        // teacher-entered values and does not show the same mock slots for every teacher.
        if (disponibiliteRepository.count() == 0) {
            System.out.println("ℹ️ Aucune disponibilité par défaut générée. Les enseignants doivent renseigner leurs créneaux via l'API user-service.");
        }

        List<Course> createdCourses = new ArrayList<>();
        for (Classe classe : classeRepository.findAll()) {
            for (UE ue : ueRepository.findAll()) {
                if (ue.getNiveau().equals(classe.getNiveau()) && courseMatchesFiliere(classe.getFiliere(), ue)) {
                    Enseignant teacher = findTeacherForUE(savedTeachers, ue);
                    if (teacher != null) {
                        String courseCode = ue.getCodeUE() + "-" + normalizeCourseCode(classe.getNomClasse());
                        if (!courseRepository.existsByCodeCourse(courseCode)) {
                            createdCourses.add(new Course(
                                    courseCode,
                                    "CM",
                                    ue.getHeuresCM(),
                                    ue.getSemestre(),
                                    "Course on " + ue.getNomUE() + " for " + classe.getNomClasse(),
                                    ue,
                                    teacher,
                                    classe
                            ));
                        }
                    }
                }
            }
        }
        if (!createdCourses.isEmpty()) {
            courseRepository.saveAll(createdCourses);
        }

        System.out.println("✅ Academic dataset enriched: " + teachers.size() + " teachers, " + classSeeds.size() + " classes, " + ueSeeds.size() + " UEs, " + courseRepository.count() + " courses.");
    }

    private Enseignant createTeacher(String idUser, String email, String nomUser, String prenomUser,
                                    String departement, String idEcole, String specialite) {
        if (utilisateurRepository.findById(idUser).isPresent() || utilisateurRepository.existsByEmail(email)) {
            return enseignantRepository.findById(idUser).orElseGet(() -> enseignantRepository.findAll().stream()
                    .filter(enseignant -> email.equalsIgnoreCase(enseignant.getEmail())).findFirst().orElse(null));
        }

        Enseignant enseignant = new Enseignant();
        enseignant.setIdUser(idUser);
        enseignant.setMdp(passwordEncoder.encode("password123"));
        enseignant.setEmail(email);
        enseignant.setNomUser(nomUser);
        enseignant.setPrenomUser(prenomUser);
        enseignant.setTelephone("650000001");
        enseignant.setRole("ENSEIGNANT");
        enseignant.setDepartement(departement);
        enseignant.setIdEcole(idEcole);
        enseignant.getIdEcoles().add(idEcole);
        enseignant.setSpecialite(specialite);
        return enseignantRepository.save(enseignant);
    }

    private Classe createClass(String nomClasse, String niveau, String filiere, String anneeAcademique, Integer nombreEtudiants) {
        Classe classe = new Classe();
        classe.setNomClasse(nomClasse);
        classe.setNiveau(niveau);
        classe.setFiliere(filiere);
        classe.setAnneeAcademique(anneeAcademique);
        classe.setNombreEtudiants(nombreEtudiants);
        classe.setDescription("Classe académique pour la filière " + filiere + " en " + anneeAcademique);
        classe.setActive(true);
        return classe;
    }

    private UE createUE(String codeUE, String nomUE, String description, Integer heuresCM, Integer heuresTD,
                       Integer heuresTP, Integer creditsECTS, String semestre, String niveau) {
        UE ue = new UE(codeUE, nomUE, description, heuresCM, heuresTD, heuresTP, creditsECTS, semestre, niveau);
        ue.setActive(true);
        return ue;
    }

    private void assignTeacherToUE(Enseignant enseignant, UE... uesForTeacher) {
        if (enseignant == null) {
            return;
        }
        for (UE ue : uesForTeacher) {
            if (ue != null && !enseignant.getUes().contains(ue)) {
                enseignant.getUes().add(ue);
                if (!ue.getEnseignants().contains(enseignant)) {
                    ue.getEnseignants().add(enseignant);
                }
            }
        }
        enseignantRepository.save(enseignant);
        for (UE ue : uesForTeacher) {
            if (ue != null) {
                ueRepository.save(ue);
            }
        }
    }

    private void seedAvailability(Enseignant enseignant, DayOfWeek jour, LocalTime heureDebut, LocalTime heureFin) {
        if (enseignant == null) {
            return;
        }
        Disponibilite disponibilite = new Disponibilite(jour, heureDebut, heureFin, enseignant);
        disponibiliteRepository.save(disponibilite);
    }

    private boolean courseMatchesFiliere(String filiere, UE ue) {
        if (filiere == null || ue == null) return false;
        String code = ue.getCodeUE();
        if ("Informatics".equalsIgnoreCase(filiere) || "Informatique".equalsIgnoreCase(filiere)) {
            return code.startsWith("INF");
        }
        if ("Business & Finance".equalsIgnoreCase(filiere) || "Business".equalsIgnoreCase(filiere)) {
            return code.startsWith("BIZ");
        }
        if ("Networking & Security".equalsIgnoreCase(filiere) || "Réseaux & Sécurité".equalsIgnoreCase(filiere) || "Réseaux".equalsIgnoreCase(filiere)) {
            return code.startsWith("RES");
        }
        if ("Civil Engineering".equalsIgnoreCase(filiere) || "Génie Civil".equalsIgnoreCase(filiere) || "Genie Civil".equalsIgnoreCase(filiere)) {
            return code.startsWith("CIV");
        }
        if ("Engineering".equalsIgnoreCase(filiere)) {
            return code.startsWith("ENG");
        }
        return false;
    }

    private Enseignant findTeacherForUE(Map<String, Enseignant> enseignants, UE ue) {
        if (ue == null || ue.getCodeUE() == null) {
            return null;
        }
        String code = ue.getCodeUE();
        if (code.startsWith("INF")) {
            return enseignants.getOrDefault("nkiamboh.tanwi@ictuniversity.org", enseignants.get("marie.mba@ictuniversity.org"));
        }
        if (code.startsWith("BIZ")) {
            return enseignants.getOrDefault("grace.ekeme@ictuniversity.org", enseignants.get("pierre.ndjock@ictuniversity.org"));
        }
        if (code.startsWith("RES")) {
            return enseignants.getOrDefault("alain.dibongo@ictuniversity.org", enseignants.get("louise.tchinda@ictuniversity.org"));
        }
        if (code.startsWith("CIV")) {
            return enseignants.getOrDefault("sophie.mokam@ictuniversity.org", enseignants.get("charles.ekoume@ictuniversity.org"));
        }
        if (code.startsWith("ENG")) {
            return enseignants.get("john.martin@ictuniversity.org");
        }
        return null;
    }

    private String normalizeCourseCode(String nomClasse) {
        return nomClasse.toUpperCase().replace(" ", "-").replace("-A", "");
    }

    private record TeacherSeed(String id, String email, String nom, String prenom, String departement, String school, String specialite) {
    }

    private record ClassSeed(String nom, String niveau, String filiere, int effectif) {
    }

    private record UESeed(String code, String nom, String semestre, String niveau, String departement, int credits, int cm, int td, int tp, String description) {
    }
}
