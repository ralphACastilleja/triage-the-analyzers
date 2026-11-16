# Slice 4: Security analyzer triage

These are agent-proposed decisions for student review. Source locations below refer
to the assignment's original `main` revision, `d397bb1`, so they remain comparable
with the original reports after edits move lines.

## Project decisions

| Decision | Choice | Reason and implementation |
|---|---|---|
| D1: `EI_EXPOSE_REP`, `EI_EXPOSE_REP2` (219) | Narrow | Exempt JPA entities, whose identity and association collection wrappers participate in persistence; exempt only constructor storage in Spring services/components/controllers, which must retain shared collaborators. Keep the mutable evaluation report builders and the principal's delegation to its user entity. Enforce collection ownership elsewhere: transfer records and the template bean. The exact scopes and reasons are in `backend/config/spotbugs-exclude.xml`. 181 findings are covered by these deliberate sharing contracts; 38 are fixed. |
| D2: `UncommentedEmptyConstructor`, `UnnecessaryConstructor` (40) | Enforce | Document 20 empty no-arg constructors that coexist with value constructors. Remove 10 redundant sole public empty constructors, resolving both warnings for each. javac supplies an equivalent public no-arg constructor for these public classes, so JPA and bean construction remain available. Do not remove a no-arg constructor merely because its body is empty when another constructor exists. Retain both rules in `pmd-ruleset.xml`. |
| D3: `ControlStatementBraces` (30) | Enforce | Add braces to all 30 single-line conditionals. Many are association helpers or security guards: a later added statement must stay within the intended condition. Preserve each condition, body, and return/continue. Retain the rule in `pmd-ruleset.xml`. |
| D4: `GuardLogStatement` (4) | Enforce | SLF4J placeholders defer formatting but not argument evaluation. Guard the four reported calls in AuthController, ReminderService, WeeklyReminderScheduler, and DocumentTemplateRegistry. Only the log statement moves inside the guard; SMTP work, failure-list updates, reminder calculations, and template loading still run independently of the log level. Retain the rule in `pmd-ruleset.xml`. |

Transfer-record constructors copy collection structure and expose unmodifiable
collections. They preserve null collections, null members, and iteration order;
using `List.copyOf`/`Set.copyOf` instead would reject null members before existing
validation runs. These are shallow collection snapshots, not clones of JPA
entities or a claim that every nested object is immutable. DocumentTemplate copies
its list on input and output, retaining its mutable bean and SectionTemplate API.
An absent template section list becomes empty; DocumentService and DataInitializer
already treat null and empty template sections identically.

## My slice

All paths below are under `backend/src/main/java/team/projectpulse/`.
The three SpotBugs entries and all six PMD findings are removed from their baselines.

| Original file:line | Rule | Verdict | Recorded as | Reason |
|---|---|---|---|---|
| security/AuthService.java:35 | NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE | False positive on the successful login path | Method-level SuppressFBWarnings with justification | AuthController requires authentication; UserService.loadUserByUsername returns MyUserPrincipal or throws. A null principal is not a successful Project Pulse login. This is an inference from that call path, not a claim that every Authentication implementation has a non-null principal. |
| security/JwtProvider.java:38 | NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE | False positive on the successful login path | Method-level SuppressFBWarnings with justification | Its only production caller is AuthService, which has already dereferenced the same authenticated MyUserPrincipal before requesting a token. Keep the nullable interface contract distinct from this caller's precondition. |
| security/SecurityConfiguration.java:130 | CT_CONSTRUCTOR_THROW | Not worth it in this application's threat model | Constructor-level SuppressFBWarnings with justification | A missing RSA provider must abort startup. This application loads trusted Spring configuration, not attacker-controlled subclasses. Making this Configuration class final would break its default Spring proxying. Catching the exception and continuing without keys would be wrong. |
| security/AuthService.java:39 | LiteralsFirstInComparisons | Real style finding | Literal-first equality | Use "student".equals(roles), keeping the existing role branches. This does not make the whole method null-role-safe: the instructor branch still calls contains. |
| security/JwtProvider.java:30 | LambdaCanBeMethodReference | Real style finding | GrantedAuthority::getAuthority | The lambda only forwards the receiver and calls the same method; token authority ordering and space delimiter stay intact. |
| security/JwtProvider.java:38 | UselessParentheses | Real style finding | Remove only parentheses around authentication.getPrincipal() | Preserve the MyUserPrincipal cast and its grouping before member access. |
| security/SecurityConfiguration.java:317 | LambdaCanBeMethodReference | Real style finding | AbstractHttpConfigurer::disable | Equivalent receiver method reference. CSRF policy is unchanged; this is not a security-policy change. |
| system/StatusCode.java:3 | InstantiableUtilityClass | Real API-design finding | Test 55c6383, fix 3269e9f | This class contains only static constants, so instances serve no purpose. A private no-arg constructor closes the public construction API; final also satisfies PMD's private-constructor class rule. |
| user/UserController.java:3 | UnnecessaryImport | Real style finding | Remove unused UserDetails import | No declaration in the controller uses UserDetails. |

The module's getAuthentication()/getAuthority() examples are outside this slice and
remain baselined. No additional nullness suppression was added for those examples.
The successful-login reasoning above does not establish that the endpoint accepts
every possible principal type: for example, a JWT principal is not MyUserPrincipal.
That broader authentication-input audit was not undertaken here.

## The bug demonstrated

`StatusCodeTest.constantsHolderDoesNotExposeAConstructor` asserts that the
constants holder's no-argument constructor is private. It fails on the implicit
public constructor and passes after the fix. This demonstrates the precise
instantiable-utility-class API defect; it does **not** demonstrate an exploitable
security vulnerability or a user-visible authentication failure.

History:

1. `55c6383`: commit the test without the implementation change.
2. Run `mvnw.cmd -Dtest=StatusCodeTest test`: one test, one assertion failure,
   expected private constructor but the public default was present.
3. `3269e9f`: make StatusCode final and give it a private constructor.
4. Run the test after the fix: one test, zero failures.

See the captured excerpts in `evidence/status-code-red.txt` and
`evidence/status-code-green.txt`. TransferCollectionOwnershipTest additionally
checks both sides of collection aliasing and preserves null-member behavior.

## Baselines

| Measure | Before | After |
|---|---:|---:|
| SpotBugs Match entries / original findings | 237 | 15 |
| PMD original violation occurrences covered | 130 | 50 |
| PMD distinct class-rule entries in the properties file | 101 | 38 |

The PMD properties file groups multiple occurrences into a single class-rule
entry, so counting its lines does not count warnings. D2-D4 remove 74 original
occurrences, and the slice removes another six. All remaining class-rule pairs are
subsets of the original pairs. SpotBugs baseline lines are deleted verbatim.

## Agent assistance and review

Codex proposed and implemented the triage, source edits, tests, configuration,
baseline deletions, and this record. The student has not yet reviewed each verdict;
this record does not claim otherwise.

During implementation, the agent rejected these approaches:

- Blanket suppression of D1. The final filter is limited to managed sharing and
  explicitly mutable report/principal contracts; transfer collections are fixed.
- Defensive copies of JPA associations or injected services, which would change
  identity/sharing rather than merely remove analyzer noise.
- Copying WeeklyPeerEvaluationReport's getter result: EvaluationService adds rows
  through that getter, so copying it without redesigning the builder loses rows.
- Removing all empty no-argument constructors. Only the redundant sole public
  constructors are removed; constructors needed alongside overloads are retained.
- Making SecurityConfiguration final or ignoring key-generation failure. The former
  breaks default configuration proxying; the latter could leave unusable JWT keys.
- Rewriting six untouched lambda baseline names after a Java 22 local run. Use the
  project's Java 21 toolchain instead; the baseline must not grow or be rewritten.

These rejections are the agent's implementation decisions, not invented claims
about changes the student personally rejected.

## Sources checked

- [SpotBugs filter documentation](https://spotbugs.readthedocs.io/en/latest/filter.html): annotation and method predicates used in the narrowed filter.
- [SpotBugs bug descriptions](https://spotbugs.readthedocs.io/en/latest/bugDescriptions.html): CT_CONSTRUCTOR_THROW describes a finalizer/subclass attack, not a blanket ban on constructor exceptions.
- [Spring Configuration contract](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/Configuration.html): default proxying requires a non-final configuration class.
- [PMD UnnecessaryConstructor](https://pmd.github.io/pmd/pmd_rules_java_codestyle.html#unnecessaryconstructor): applies to a sole constructor equivalent to the implicit default.

## Verification limits

The regression test was observed red before the fix and green afterward. The
final Java 21 gate passed both analyzers and 129 selected unit tests:
`mvnw.cmd verify -Dtest=*Test,!*IntegrationTest,!*ControllerTest,!WebConfigSpaForwardingTest`.
The exclusions avoid Docker-backed tests; all test sources still compile.
See `evidence/java21-gate.txt` for the result excerpt. A preceding local Java 22
analyzer run failed on six compiler-generated lambda names that differ from the
Java 21 baseline; that run is not claimed as a passing gate. CI results are recorded
in the pull request when available.

Docker is not installed locally; the MySQL/Mailpit integration tests must run in
GitHub Actions. Local frontend checks were not run because npm/npx are unavailable;
the unchanged report-only CI steps provide those results. No browser behavior,
load test, comprehensive security audit, or independent reading of the course's
Static Analysis module was verified.
