import zipfile
from pathlib import Path

out_path = Path(r'c:\planora\Plannora_UML_Document.docx')

sections = [
    'Plannora - UML diagrams and agile workflow',
    '',
    '1. Context and project scope',
    'Plannora is a university scheduling and room reservation application for an ICT university.',
    'The platform supports administrators, teachers, and students. It manages authentication, teaching units (UE), classes, rooms, availability, timetable generation, reservations, and notifications.',
    '',
    'Main modules in the actual project:',
    '- Authentication Service: login, registration, JWT security, password reset',
    '- User Service: users, teachers, classes, UE assignments',
    '- UE Service: teaching units and academic structure',
    '- Classes Service: filiere, specialite, class management',
    '- Planning Service: availability and timetable generation',
    '- Salle Service: room management and reservation logic',
    '- Notification Service: alerts and notifications',
    '- Gateway Service: routing and security',
    '- Frontend Angular: role-based user interface',
    '',
    '2. Use case diagram',
    'Admin: login, manage users, manage UE, manage classes, manage rooms, generate timetable',
    'Teacher: login, view timetable, set availability, assign UE, publish schedule',
    'Student: login, view timetable, reserve room, consult schedule, receive notifications',
    'System: authenticate, validate roles, manage academic data, plan courses, check room availability, resolve conflicts, notify users',
    '',
    '3. Class diagram',
    'User { id, email, nom, prenom, password, telephone, role }',
    'Administrateur { manageSystem(), validateSchedule(), manageUsers() }',
    'Enseignant { assignUE(), setAvailability(), teachCourse() }',
    'Etudiant { reserveRoom(), viewTimetable(), viewSchedule() }',
    'UE { id, codeUE, nomUE, creditsECTS, semestre, niveau }',
    'Classe { id, nom, niveau, filiere, effectif }',
    'Salle { id, nom, capacite, type, disponible }',
    'EmploiDuTemps { id, dateJour, heureDebut, heureFin, ue, classe, salle, typeCours, enseignantId }',
    'Disponibilite { id, enseignantId, dateHeureDebut, dateHeureFin, commentaire }',
    'Notification { id, userId, message, type, send() }',
    '',
    '4. Sequence diagram - planning process',
    'Admin -> System: Request timetable generation',
    'System -> Teacher: Read availability data',
    'Teacher --> System: Submit availability slots',
    'System -> UE: Load assigned teaching units',
    'System -> Classe: Load class groups',
    'System -> Salle: Check room availability',
    'System -> System: Validate constraints',
    'alt conflicting schedule',
    'System --> Admin: Show conflict warning',
    'Admin -> System: Adjust constraints',
    'else schedule valid',
    'System -> EmploiDuTemps: Create timetable entries',
    'System -> Notification: Notify teachers and students',
    'System --> Admin: Timetable generated',
    'end',
    '',
    '5. Sequence diagram - authentication logic',
    'User -> Frontend: Enter email and password',
    'Frontend -> Gateway: POST /api/auth/login',
    'Gateway -> AuthService: Forward credentials',
    'AuthService -> UserRepository: findByEmail(email)',
    'UserRepository --> AuthService: user record',
    'AuthService -> PasswordEncoder: Validate password hash',
    'PasswordEncoder --> AuthService: valid / invalid',
    'alt valid credentials',
    'AuthService -> JwtUtil: Generate token',
    'JwtUtil --> AuthService: JWT token',
    'AuthService --> Frontend: LoginResponse(token, role, user info)',
    'Frontend -> Router: Redirect by role',
    'else invalid credentials',
    'AuthService --> Frontend: Error message',
    'end',
    '',
    '6. Activity diagram - business process',
    '[Start] -> Login',
    'Login -> Check role',
    'Check role -> Admin?',
    'Admin? -> Yes -> Manage users, UE, classes, rooms',
    'Admin? -> No -> Teacher or Student path',
    'Teacher path -> Set availability',
    'Teacher path -> View assignments',
    'Student path -> View timetable',
    'Set availability -> Save availability',
    'Save availability -> Generate timetable',
    'Generate timetable -> Check conflicts',
    'Check conflicts -> Conflicts -> Modify schedule',
    'Check conflicts -> No conflicts -> Publish timetable',
    'Publish timetable -> Notify users',
    'Notify users -> [End]',
    '',
    '7. Activity diagram - logic flow',
    '[Start] -> Receive request',
    'Receive request -> Validate input',
    'Validate input -> Email domain valid?',
    'Email domain valid? -> No -> Reject',
    'Email domain valid? -> Yes -> Find user',
    'Find user -> User exists?',
    'User exists? -> No -> Reject',
    'User exists? -> Yes -> Validate password',
    'Validate password -> No -> Reject',
    'Validate password -> Yes -> Generate JWT',
    'Generate JWT -> Return token',
    'Return token -> [End]',
    '',
    '8. UML system overview',
    'Angular Frontend -> API Gateway -> Auth Service -> User Repository',
    'User Service -> UE Service -> Classes Service',
    'Planning Service -> Salle Service -> Notification Service',
    'Architecture follows a microservice pattern with role-based access and domain separation.',
    '',
    '9. Agile diagram / sprint steps',
    '[Project kickoff] -> Define requirements -> Identify actors -> Define academic constraints',
    '[Sprint 1] -> Authentication and base user management',
    '[Sprint 2] -> Academic data management (UE, classes, teachers)',
    '[Sprint 3] -> Planning and availability management',
    '[Sprint 4] -> Room reservation and notifications',
    '[Sprint 5] -> Angular frontend and integration',
    '[Sprint 6] -> Testing, deployment, and acceptance',
    '',
    '10. Summary',
    'Plannora follows a modular microservice architecture focused on academic planning, scheduling, and room allocation.',
    'The main business flow is: login -> validate role -> manage academic data -> set availability -> generate timetable -> resolve conflicts -> publish and notify.',
    '',
    'Generated for the current Plannora workspace project.'
]

# Build minimal DOCX package manually.
paragraphs = []
for line in sections:
    if not line:
        paragraphs.append('<w:p/>')
    else:
        escaped = (line.replace('&', '&amp;')
                       .replace('<', '&lt;')
                       .replace('>', '&gt;'))
        paragraphs.append(f'<w:p><w:r><w:t xml:space="preserve">{escaped}</w:t></w:r></w:p>')

doc_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    %s
    <w:sectPr>
      <w:pgSz w:w="12240" w:h="15840"/>
      <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/>
    </w:sectPr>
  </w:body>
</w:document>
''' % ''.join(paragraphs)

content_types_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>
'''

rels_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>
'''

core_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>Plannora UML Document</dc:title>
  <dc:creator>GitHub Copilot</dc:creator>
  <cp:lastModifiedBy>GitHub Copilot</cp:lastModifiedBy>
  <dcterms:created xsi:type="dcterms:W3CDTF">2026-10-21T00:00:00Z</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">2026-10-21T00:00:00Z</dcterms:modified>
</cp:coreProperties>
'''

app_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Microsoft Office Word</Application>
</Properties>
'''

with zipfile.ZipFile(out_path, 'w', compression=zipfile.ZIP_DEFLATED) as zf:
    zf.writestr('[Content_Types].xml', content_types_xml)
    zf.writestr('_rels/.rels', rels_xml)
    zf.writestr('docProps/core.xml', core_xml)
    zf.writestr('docProps/app.xml', app_xml)
    zf.writestr('word/document.xml', doc_xml)

print(f'Created: {out_path}')
