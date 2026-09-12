import pathlib
import sys

fp = pathlib.Path('C:/shree-ai-os/src/main/java/com/shreeai/os/platform/kernels/inference/engine/DefaultInferenceEngine.java')
lines = fp.read_text().split('\n')

method_start = None
for i, line in enumerate(lines):
    if 'public InferenceResult infer(' in line and method_start is None:
        method_start = i
        break

if method_start is None:
    print('Could not find infer method')
    sys.exit(1)

brace_count = 0
insert_at = None
started = False
for i in range(method_start, len(lines)):
    opens = lines[i].count('{')
    closes = lines[i].count('}')
    if opens > 0:
        started = True
    brace_count += opens - closes
    if started and brace_count == 0:
        insert_at = i + 1
        break

print(f'Insert at line {insert_at}')

with open('C:/shree-ai-os/new_methods_part1.txt', 'w') as f:
    f.write('''
    /**
     * Performs evidence-based inference using a pre-resolved EvidencePackage
     * (P0.3 deterministic conflict resolution). This method consumes the
     * resolved EvidencePackage from CognitiveState, never raw conflicting evidence.
     *
     * @param request the original user request
     * @param reasoningResult the upstream reasoning result
     * @param evidencePackage the resolved evidence package (must not be null)
     * @param context execution context
     * @return inference result
     * @throws NullPointerException if evidencePackage is null
     */
    public InferenceResult infer(
            String request,
            ReasoningResult reasoningResult,
            EvidencePackage evidencePackage,
            String context) {

        Objects.requireNonNull(evidencePackage, "evidencePackage must not be null");

        String inferenceId = "inf-" + UUID.randomUUID().toString().substring(0, 8);

        String normalizedRequest = normalize(request);
        String normalizedContext = normalize(context);

        List<ResolvedFact> resolvedFacts = evidencePackage.resolvedFacts();
        List<String> supportingEvidence = new ArrayList<>();
        List<String> contradictingEvidence = new ArrayList<>();
        List<String> unknownInformation = new ArrayList<>();

        for (ResolvedFact fact : resolvedFacts) {
            supportingEvidence.add(fact.fact());
        }

        contradictingEvidence.addAll(evidencePackage.discardedEvidence());

        for (ConflictRecord conflict : evidencePackage.conflicts()) {
            for (String rejected : conflict.rejectedFacts()) {
                unknownInformation.add("Conflicting evidence rejected: " + rejected);
            }
        }

        Set<String> requestTerms = extractTerms(normalizedRequest);

        List<Hypothesis> hypotheses = generateHypothesesFromPackage(
                normalizedRequest,
                reasoningResult,
                resolvedFacts,
                evidencePackage,
                requestTerms
        );

        hypotheses = rankHypotheses(hypotheses);

        Hypothesis bestHypothesis;
        if (hypotheses.isEmpty()) {
            bestHypothesis = new Hypothesis(
                    "h0",
                    "Insufficient evidence to form a reliable hypothesis",
                    0.10,
                    List.of(),
                    List.of("No sufficiently relevant evidence was available"),
                    "UNLIKELY",
                    0
            );
            hypotheses = List.of(bestHypothesis);
        } else {
            bestHypothesis = hypotheses.get(0);
        }

        double overallConfidence = evidencePackage.overallConfidence();

        if (resolvedFacts.isEmpty()) {
            unknownInformation.add("No resolved evidence available");
        }

        String investigationContext = normalizedContext.isBlank()
                ? "inference"
                : normalizedContext;

        String nextInvestigation = recommendNextInvestigationFromPackage(
                normalizedRequest,
                bestHypothesis,
                unknownInformation,
                overallConfidence,
                evidencePackage
        );

        return new InferenceResult(
                inferenceId,
                hypotheses,
                bestHypothesis,
                clamp(overallConfidence, 0.0, 1.0),
                deduplicate(supportingEvidence),
                deduplicate(contradictingEvidence),
                deduplicate(unknownInformation),
                nextInvestigation,
                investigationContext,
                Instant.now()
        );
    }
''')

with open('C:/shree-ai-os/new_methods_part2.txt', 'w') as f:
    f.write('''
    /**
     * Generates hypotheses from the resolved evidence package.
     */
    private List<Hypothesis> generateHypothesesFromPackage(
            String request,
            ReasoningResult reasoningResult,
            List<ResolvedFact> resolvedFacts,
            EvidencePackage evidencePackage,
            Set<String> requestTerms) {

        Objects.requireNonNull(resolvedFacts, "resolvedFacts must not be null");

        List<Hypothesis> hypotheses = new ArrayList<>();
        int index = 0;

        String conclusion = reasoningResult != null ? reasoningResult.conclusion() : "";
        double reasoningConfidence = reasoningConfidence(reasoningResult);

        List<String> supporting = new ArrayList<>();
        List<String> opposing = new ArrayList<>();

        for (ResolvedFact fact : resolvedFacts) {
            if (fact.confidence() > 0.5) {
                supporting.add(fact.fact() + ": " + fact.value());
            } else {
                opposing.add(fact.fact() + ": " + fact.value());
            }
        }

        if (!conclusion.isBlank()) {
            hypotheses.add(new Hypothesis(
                    "h" + index,
                    conclusion,
                    clamp(reasoningConfidence * 0.6 + evidencePackage.overallConfidence() * 0.4, 0.0, 1.0),
                    supporting,
                    opposing,
                    statusFor(reasoningConfidence),
                    index
            ));
            index++;
        }

        for (ResolvedFact fact : resolvedFacts) {
            hypotheses.add(new Hypothesis(
                    "h" + index,
                    fact.value(),
                    clamp(fact.confidence(), 0.0, 1.0),
                    List.of(fact.fact()),
                    List.of(),
                    statusFor(fact.confidence()),
                    index
            ));
            index++;
        }

        return hypotheses;
    }
''')

with open('C:/shree-ai-os/new_methods_part3.txt', 'w') as f:
    f.write('''
    /**
     * Recommends the next investigation based on the evidence package.
     */
    private String recommendNextInvestigationFromPackage(
            String request,
            Hypothesis bestHypothesis,
            List<String> unknownInformation,
            double confidence,
            EvidencePackage evidencePackage) {

        if (confidence < 0.60) {
            return "Gather additional evidence to increase confidence in the conclusion";
        }
        if (!unknownInformation.isEmpty()) {
            return "Address unknown information: " + unknownInformation.get(0);
        }
        if (evidencePackage.conflicts().isEmpty()) {
            return "No conflicts detected — conclusion is well-supported";
        }
        return "Evidence package resolved with " + evidencePackage.conflicts().size()
                + " conflict(s) — review discarded evidence";
    }
''')

p1 = pathlib.Path('C:/shree-ai-os/new_methods_part1.txt').read_text().split('\n')
p2 = pathlib.Path('C:/shree-ai-os/new_methods_part2.txt').read_text().split('\n')
p3 = pathlib.Path('C:/shree-ai-os/new_methods_part3.txt').read_text().split('\n')

all_new = p1 + p2 + p3
for i, nl in enumerate(all_new):
    lines.insert(insert_at + i, nl)

# Add imports
for j, line in enumerate(lines):
    if 'import com.shreeai.os.platform.kernels.inference.model.InferenceResult;' in line:
        if 'ConflictRecord' not in lines[j+1] and 'ConflictRecord' not in lines[j+2]:
            lines.insert(j + 1, 'import com.shreeai.os.platform.kernels.inference.model.ConflictRecord;')
            lines.insert(j + 1, 'import com.shreeai.os.platform.kernels.inference.model.ResolvedFact;')
        break

fp.write_text('\n'.join(lines))
print(f'Done. File now has {len(lines)} lines')
