package de.jungblut.kaggle.asap;

import com.google.common.collect.Lists;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import weka.core.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * @author thomas.jungblut
 */
@SuppressWarnings({"unchecked", "deprecation", "rawtypes"})
final class Vectorizer {

    private static final int NUM_PROCESSORS = 5;
    static final ExecutorService threadPool = Executors.newCachedThreadPool();

    // essay_set -> min and max domain 1 score
    private static final HashMap<String, Integer> maxScore = new HashMap<>();
    private static final HashMap<String, Integer> minScore = new HashMap<>();

    // top 1000 idf scored tokens in training set
    private static final HashMap<String, Double> idfMap = new HashMap<>();
    // top idf scored tokens per sentence from essay
    private static final HashMap<Integer, HashSet<String>> idfEssayMap = new HashMap<>();

    private static final HashMap<String, Integer> sentimentMap = new HashMap<>();

    private static final Pattern specialSignFilter = Pattern.compile(
            "\\p{Punct}", Pattern.CASE_INSENSITIVE);
    private static final Pattern upperCaseStartsWith = Pattern
            .compile("^[A-Z]");
    // how many polynoms to add? starting from 2.
    private static final int POLYNOMIAL_ADD = 50;

    static {
        minScore.put("1", 2);
        maxScore.put("1", 12);
        minScore.put("2", 1);
        maxScore.put("2", 6);
        minScore.put("-2", 1);
        maxScore.put("-2", 4);
        minScore.put("3", 0);
        maxScore.put("3", 3);
        minScore.put("4", 0);
        maxScore.put("4", 3);
        minScore.put("5", 0);
        maxScore.put("5", 4);
        minScore.put("6", 0);
        maxScore.put("6", 4);
        minScore.put("7", 0);
        maxScore.put("7", 30);
        minScore.put("8", 0);
        maxScore.put("8", 60);

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(new File(
                    "files/tfidf/idf.csv")));
            String line;
            while ((line = br.readLine()) != null) {
                final String[] split = line.split("\t");
                idfMap.put(split[0], Double.valueOf(split[1]));
                if (idfMap.size() >= 5000)
                    break;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 3; i <= 6; i++) {
            HashSet<String> set = new HashSet<>();
            idfEssayMap.put(i, set);
            br = null;
            try {
                br = new BufferedReader(new FileReader(new File(
                        "files/tfidf/idf_essay_" + i + ".csv")));
                String line;
                while ((line = br.readLine()) != null) {
                    final String[] split = line.split("\t");
                    set.add(split[0]);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        br = null;
        try {
            br = new BufferedReader(new FileReader(new File(
                    "files/AFINN/AFINN-111.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                final String[] split = line.split("\t");
                sentimentMap.put(split[0], Integer.parseInt(split[1]));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Instances trainingSet;
    private static Instances testSet;

    /**
     * Features TODO: maybe put into another model and use boosting?<br>
     * -sentence construction, does a verb follow a noun? etc.<br>
     * -subject-verb agreement (The subject and verb must agree in number: both
     * must be singular, or both must be plural.) <br>
     * -spelling -number of nouns/verbs weighted per sentence and variance of it <br>
     * -difference between scorers? <br>
     * -clustering for very "intense" words for each score in each essayset <br>
     * -classification of "theme". Large diff of prediction.. <br>
     * TODO we are facing lots of off-by-one errors. Maybe another stump or
     * ZeroR can help to vote the right result..
     */
    public final ArrayList<Attribute> attributes = new ArrayList<>();
    private final Attribute textLength = new Attribute("Text length (chars)");
    private final Attribute logTextLength = new Attribute(
            "Logarithmic text length (chars)");
    private final Attribute textLengthTokens = new Attribute(
            "Text length (tokens)");
    private final Attribute logTextLengthTokens = new Attribute(
            "Logarithmic text length (tokens)");
    private final Attribute numVerbs = new Attribute("Number of verbs");
    private final Attribute numNouns = new Attribute("Number of nouns");
    private final Attribute verbToNounRatio = new Attribute(
            "Ratio between verb and nouns");
    private final Attribute numAdjectives = new Attribute(
            "Number of adjectives");
    private final Attribute adjectiveToNounRatio = new Attribute(
            "Ratio between adjective and nouns");
    private final Attribute adjectiveToVerbRatio = new Attribute(
            "Ratio between adjective and verb");
    private final Attribute numCommas = new Attribute("Number of comma");
    private final Attribute numDots = new Attribute("Number of dots");
    private final Attribute logNumCommas = new Attribute("Log Number of comma");
    private final Attribute logNumDots = new Attribute("Log Number of dots");
    private final Attribute numQuestion = new Attribute(
            "Number of question marks");
    private final Attribute numAts = new Attribute("Number of ats");
    private final Attribute logNumAts = new Attribute("Log Number of ats");
    private final Attribute numExclam = new Attribute(
            "Number of exclamation marks");
    private final Attribute avgTokenLength = new Attribute(
            "Average token length");
    private final Attribute tokenLengthVariance = new Attribute(
            "Variance of token length");
    private final Attribute highValuableTokensCount = new Attribute(
            "Count of highly valuable tokens used");
    private final Attribute highValuableTokensPerToken = new Attribute(
            "Ratio of highly valuable tokens to all tokens");
    private final Attribute numSentences = new Attribute("Number of sentences");
    private final Attribute tokenPerSentence = new Attribute(
            "Number of tokens per sentence");
    private final Attribute logTokenPerSentence = new Attribute(
            "Logarithmic number of tokens per sentence");
    private final Attribute duplicateWordScore = new Attribute(
            "Score of duplicate words");
    private final Attribute maxDuplicateWordScore = new Attribute(
            "Max Score of duplicate words");
    private final Attribute wordsContainedInEssay = new Attribute(
            "Words contained in essay");
    private final Attribute logWordsContainedInEssay = new Attribute(
            "Log words contained in essay");
    private final Attribute sentenceSameBeginCount = new Attribute(
            "Count of how many sentences have the same beginning");
    private final Attribute logSentenceSameBeginCount = new Attribute(
            "Log count of how many sentences have the same beginning");
    private final Attribute capitalizationPerSentence = new Attribute(
            "Count how many capitalized tokens per sentence");
    private final Attribute logCapitalizationPerSentence = new Attribute(
            "Log count how many capitalized tokens per sentence");
    private final Attribute sumSentiment = new Attribute(
            "Sum of sentiment of the text");
    private final Attribute weightedSentiment = new Attribute(
            "Square Weighted sum of sentiment of the text");
    private final Attribute logWeightedSentiment = new Attribute(
            "Log Weighted sum of sentiment of the text");

    public Attribute prediction = null;
    private final int filter;
    int mode = 0;
    private final int attributeIndexStartPolys;
    private final HashSet<String> idfTokensPerEssay;

    {
        attributes.add(textLength);
        attributes.add(logTextLength);
        attributes.add(textLengthTokens);
        attributes.add(logTextLengthTokens);
        attributes.add(numVerbs);
        attributes.add(numNouns);
        attributes.add(verbToNounRatio);
        attributes.add(numAdjectives);
        attributes.add(adjectiveToNounRatio);
        attributes.add(adjectiveToVerbRatio);
        attributes.add(numCommas);
        attributes.add(numDots);
        attributes.add(logNumCommas);
        attributes.add(logNumDots);
        attributes.add(numQuestion);
        attributes.add(numAts);
        attributes.add(logNumAts);
        attributes.add(numExclam);
        attributes.add(avgTokenLength);
        attributes.add(tokenLengthVariance);
        attributes.add(highValuableTokensCount);
        attributes.add(highValuableTokensPerToken);
        attributes.add(numSentences);
        attributes.add(tokenPerSentence);
        attributes.add(logTokenPerSentence);
        attributes.add(duplicateWordScore);
        attributes.add(maxDuplicateWordScore);
        attributes.add(wordsContainedInEssay);
        attributes.add(logWordsContainedInEssay);
        attributes.add(sentenceSameBeginCount);
        attributes.add(logSentenceSameBeginCount);
        attributes.add(capitalizationPerSentence);
        attributes.add(logCapitalizationPerSentence);
        attributes.add(sumSentiment);
        attributes.add(weightedSentiment);
        attributes.add(logWeightedSentiment);
        attributeIndexStartPolys = attributes.size();

        ArrayList<Attribute> tmpList = new ArrayList<>();
        for (Attribute attribute : attributes) {
            for (int i = 0; i < POLYNOMIAL_ADD; i++) {
                tmpList.add(new Attribute(attribute.name() + "^" + (i + 2)));
            }
        }

        attributes.addAll(tmpList);
    }

    public Vectorizer(int filter) {
        this.filter = filter;
        this.setupPrediction();
        idfTokensPerEssay = idfEssayMap.get(filter);
    }

    /**
     * INIT finish
     */

    public final void mapFilesIntoRAM() throws IOException,
            InterruptedException, ExecutionException {
        BufferedReader br = new BufferedReader(new FileReader(new File(
                "files/training_set_rel3.csv")));
        List<String[]> set = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null)
            set.add(line.split("\t"));

        set.remove(0);
        br.close();

        set = filter(set, filter + "");

        trainingSet = extract(set);
        testSet = splitRandomSets(0.1f, trainingSet);
    }

    void setupPrediction() {
        FastVector filteredAttributeValues = new FastVector();
        final int min = minScore.get(filter + "");
        final int max = maxScore.get(filter + "");
        for (int i = min; i <= max; i++)
            filteredAttributeValues.add(i + "");

        prediction = new Attribute("Domain score", filteredAttributeValues);
        attributes.add(prediction);
    }

    public static List<String[]> filter(List<String[]> set, String s) {
        final Iterator<String[]> iterator = set.iterator();
        while (iterator.hasNext()) {
            final String[] next = iterator.next();
            if (!next[1].equals(s)) {
                iterator.remove();
            }
        }
        return set;
    }

    final Instances splitRandomSets(float percentage, Instances set) {
        if (percentage < 0.0f || percentage > 1.0f) {
            throw new IllegalArgumentException(
                    "Percentage must be between 0.0 and 1.0! Given "
                            + percentage);
        }

        Instances instances = new Instances("asap-test", attributes, set.size());
        instances.setClassIndex(attributes.size() - 1);

        final Random rand = new Random(System.nanoTime());
        final int firstMatrixRowsCount = Math.round(percentage * set.size());

        final HashSet<Integer> lowerMatrixRowIndices = new HashSet<>();
        int missingRows = firstMatrixRowsCount;
        while (missingRows > 0) {
            final int nextIndex = rand.nextInt(set.size());
            if (lowerMatrixRowIndices.add(nextIndex)) {
                missingRows--;
                final Instance removed = set.get(nextIndex);
                instances.add(removed);
            }
        }
        return instances;
    }

    public final Instance parseInstance(String[] line, POSTaggerME tagger,
                                        SentenceDetectorME sentence) {
        return addScore(line, parseInstanceInternal(line, tagger, sentence));
    }

    public final Instance parseInstanceInternal(String[] line,
                                                POSTaggerME tagger, SentenceDetectorME sentenceDetector) {
        Instance instance = new DenseInstance(attributes.size());
        String essay = specialSignFilter.matcher(line[2].trim()).replaceAll("");
        final String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(essay);
        HashMap<String, Integer> wordCount = new HashMap<>();
        int highPrecendenceTokenCount = 0;
        int idfTokensPerEssayCount = 0;
        int uppercaseTokens = 0;
        int sentiment = 0;
        for (String s : tokens) {
            final Double idf = idfMap.get(s);
            if (idf != null) {
                highPrecendenceTokenCount++;
            }
            final Integer count = wordCount.get(s);
            if (count == null) {
                wordCount.put(s, 1);
            } else {
                wordCount.put(s, count + 1);
            }
            if (idfTokensPerEssay != null) {
                if (idfTokensPerEssay.contains(s))
                    idfTokensPerEssayCount++;
            }
            if (upperCaseStartsWith.matcher(s).matches())
                uppercaseTokens++;

            final Integer sent = sentimentMap.get(s.toLowerCase());
            if (sent != null)
                sentiment += sent;
        }
        final String[] taggedTokens = tagger.tag(tokens);
        String sentences[] = sentenceDetector.sentDetect(line[2]);
        int startsSame = 0;
        for (String sentence : sentences) {
            for (String sentence2 : sentences) {
                if (sentence != sentence2
                        && sentence.startsWith(sentence2.split(" ")[0])) {
                    startsSame++;
                }
            }
        }

        final int numVerbs = countVerbs(taggedTokens);
        final int numNouns = countNouns(taggedTokens);
        final double avgTokenLength = averageTokenLength(tokens);
        final double tokenLengthVariance = tokenLengthVariance(tokens,
                avgTokenLength);
        final int numAdjectives = countAdjectives(taggedTokens);
        final double tokenPerSentence = ((double) tokens.length)
                / sentences.length;

        int maximumFreq = 1;
        int notOnes = 1;
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (maximumFreq < entry.getValue()) {
                maximumFreq = entry.getValue();
            }
            if (entry.getValue() > 1)
                notOnes++;
        }
        final double weightedWord = Math.log((double) notOnes / tokens.length);
        final double weightedMaxWord = Math.log((double) maximumFreq / notOnes);
        int commas = count(line[2], ',');
        int dots = count(line[2], '.');
        int ats = count(line[2], '@');

        instance.setValue(this.textLength, essay.length());
        instance.setValue(this.logTextLength, Math.log(essay.length()));
        instance.setValue(this.textLengthTokens, tokens.length);
        instance.setValue(this.logTextLengthTokens, Math.log(tokens.length));
        instance.setValue(this.numNouns, numNouns);
        instance.setValue(this.numVerbs, numVerbs);
        instance.setValue(this.numAdjectives, numAdjectives);
        instance.setValue(this.verbToNounRatio, ((double) numVerbs) / numNouns);
        instance.setValue(this.adjectiveToNounRatio, ((double) numAdjectives)
                / numNouns);
        instance.setValue(this.adjectiveToVerbRatio, ((double) numAdjectives)
                / numVerbs);
        instance.setValue(this.numCommas, commas);
        instance.setValue(this.numDots, dots);
        instance.setValue(this.logNumCommas, Math.log(commas + 1));
        instance.setValue(this.logNumDots, Math.log(dots + 1));
        instance.setValue(this.logNumAts, Math.log(ats + 1));
        instance.setValue(this.numAts, ats);
        instance.setValue(this.numQuestion, count(line[2], '?'));
        instance.setValue(this.numExclam, count(line[2], '!'));
        instance.setValue(this.avgTokenLength, avgTokenLength);
        instance.setValue(this.tokenLengthVariance, tokenLengthVariance);
        instance.setValue(this.highValuableTokensCount,
                highPrecendenceTokenCount);
        instance.setValue(this.highValuableTokensPerToken,
                ((double) highPrecendenceTokenCount) / tokens.length);
        instance.setValue(this.numSentences, sentences.length);
        instance.setValue(this.tokenPerSentence, tokenPerSentence);
        instance.setValue(this.logTokenPerSentence, Math.log(tokenPerSentence));
        instance.setValue(this.duplicateWordScore, weightedWord);
        instance.setValue(this.maxDuplicateWordScore, weightedMaxWord);
        instance.setValue(this.wordsContainedInEssay,
                idfTokensPerEssayCount + 1);
        instance.setValue(this.logWordsContainedInEssay,
                Math.log(idfTokensPerEssayCount + 1));
        instance.setValue(this.sentenceSameBeginCount, startsSame);
        instance.setValue(this.logSentenceSameBeginCount,
                Math.log(startsSame + 1));
        instance.setValue(this.capitalizationPerSentence,
                ((double) uppercaseTokens) / sentences.length);
        instance.setValue(this.logCapitalizationPerSentence,
                Math.log(((double) uppercaseTokens + 1) / sentences.length));
        instance.setValue(this.sumSentiment, sentiment);
        instance.setValue(this.weightedSentiment,
                ((double) sentiment) / Math.sqrt(tokens.length));
        instance.setValue(this.logWeightedSentiment, ((double) sentiment)
                / Math.log(tokens.length));

        int offset = 0;
        for (int i = 0; i < attributeIndexStartPolys; i++) {
            for (int poly = 0; poly < POLYNOMIAL_ADD; poly++) {
                double pow = Math.pow(instance.value(i), poly + 2);
                instance.setValue(attributeIndexStartPolys + offset + poly, pow);
            }
            offset += POLYNOMIAL_ADD;
        }
        return instance;
    }

    public final Instance addScore(String[] line, Instance instance) {
        String stringScore;
        if (filter != 2) {
            stringScore = line[6];
        } else {
            stringScore = line[6];
            if (mode == 1)
                stringScore = line[9];
        }
        final int realScore = Integer.parseInt(stringScore);
        final int min = minScore.get(filter + "");
        final double score = realScore - min;
        instance.setValue(this.prediction, score);
        return instance;
    }

    /**
     * SQRT((SUM(tokens => if(token.length > 1)
     * tokens.length^2)/tokens.length)-avg^2)
     */
    private double tokenLengthVariance(String[] tokens,
                                             double avgTokenLength) {
        double variance = 0.0d;
        int scored = 0;
        for (String s : tokens) {
            variance += (s.length() * s.length());
            scored++;
        }
        return Math.sqrt((variance / scored)
                - (avgTokenLength * avgTokenLength));
    }

    private double averageTokenLength(String[] tokens) {
        double avg = 0.0d;
        int scored = 0;
        for (String s : tokens) {
            avg += s.length();
            scored++;
        }
        return avg / scored;
    }

    final int count(String sourceString, char lookFor) {
        int count = 0;
        for (int i = 0; i < sourceString.length(); i++) {
            final char c = sourceString.charAt(i);
            if (c == lookFor) {
                count++;
            }
        }
        return count;
    }

    private int countVerbs(String[] taggedTokens) {
        int count = 0;
        for (String s : taggedTokens) {
            if (s.charAt(0) == 'V') {
                count++;
            }
        }
        return count;
    }

    private int countAdjectives(String[] taggedTokens) {
        int count = 0;
        for (String s : taggedTokens) {
            if (s.charAt(0) == 'J') {
                count++;
            }
        }
        return count;
    }

    private int countNouns(String[] taggedTokens) {
        int count = 0;
        for (String s : taggedTokens) {
            if (s.charAt(0) == 'N') {
                count++;
            }
        }
        return count;
    }

    public final Instances extractTrainingset() {
        return trainingSet;
    }

    public final Instances extractTestset() {
        return testSet;
    }

    final Instances extract(List<String[]> set)
            throws InterruptedException, ExecutionException, IOException {
        final List<List<String[]>> partitions = Lists.partition(set,
                (set.size() / NUM_PROCESSORS));
        // System.out.println("Partioned dataset into " + partitions.size());
        ExecutorCompletionService<Instances> completionService = new ExecutorCompletionService<>(
                threadPool);
        Instances instances = new Instances("asap", attributes, set.size());
        instances.setClassIndex(attributes.size() - 1);
        int count = 0;
        for (List<String[]> sublist : partitions) {
            final POSTaggerME tagger = getPosTagger();
            final SentenceDetectorME sentenceDetector = getSentenceDetector();
            final Instances allocatedInstance = new Instances(count + "",
                    attributes, sublist.size());
            allocatedInstance.setClassIndex(attributes.size() - 1);
            completionService.submit(new Worker(this, sublist,
                    allocatedInstance, tagger, sentenceDetector));
            count++;
        }

        for (int i = 0; i < count; i++) {
            final Future<Instances> instancesFuture = completionService.take();
            instances.addAll(instancesFuture.get());
            // System.out.println("Progress: "
            // + ((float) instances.size() / set.size() * 100) + "%");
        }
        return instances;
    }

    public final POSTaggerME getPosTagger() throws IOException {
        POSTaggerME tagger;
        try (InputStream modelIn = new FileInputStream("files/postagger/en-pos-maxent.bin")) {
            POSModel posModel = new POSModel(modelIn);
            tagger = new POSTaggerME(posModel);
        }

        return tagger;
    }

    public final SentenceDetectorME getSentenceDetector() throws IOException {
        SentenceDetectorME detector;
        try (InputStream modelIn = new FileInputStream("files/postagger/en-sent.bin")) {
            SentenceModel model = new SentenceModel(modelIn);
            detector = new SentenceDetectorME(model);
        }

        return detector;
    }

}
