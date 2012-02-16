package de.jungblut.kaggle.asap;

import de.jungblut.weka.ClassificationTask;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author thomas.jungblut
 */
public abstract class GlobalModel implements ClassificationTask {

    private static final Pattern specialSignFilter = Pattern.compile(
            "\\p{Punct}", Pattern.CASE_INSENSITIVE);
    private static final Pattern upperCaseStartsWith = Pattern
            .compile("^[A-Z]");

    private int minScore;
    private int maxScore;

    private HashSet<String> idfTokensPerEssay = new HashSet<>();
    // top 1000 idf scored tokens in training set
    private static final HashMap<String, Double> idfMap = new HashMap<>();
    // top idf scored tokens per sentence from essay
    private static final HashMap<Integer, HashSet<String>> idfEssayMap = new HashMap<>();

    private static final HashMap<String, Integer> sentimentMap = new HashMap<>();

    static {
        try (BufferedReader
                     br = new BufferedReader(new FileReader(new File(
                "files/tfidf/idf.csv")))) {
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
            try (BufferedReader
                         br = new BufferedReader(new FileReader(new File(
                    "files/tfidf/idf_essay_" + i + ".csv")))) {
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

        try (BufferedReader br = new BufferedReader(new FileReader(new File(
                "files/AFINN/AFINN-111.txt")))) {
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
    private final int attributeIndexStartPolys;

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
            for (int i = 0; i < getPolynomsToAdd(); i++) {
                tmpList.add(new Attribute(attribute.name() + "^" + (i + 2)));
            }
        }

        attributes.addAll(tmpList);
    }


    public int getMinScore() {
        return minScore;
    }

    protected void setMinScore(int minScore) {
        this.minScore = minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    protected void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }


    protected abstract int getPolynomsToAdd();

    protected void fillPrediction() {
        ArrayList<String> nominalPrediction = new ArrayList<>();
        for (int i = minScore; i <= maxScore; i++) {
            nominalPrediction.add(i + "");
        }
        Attribute prediction = new Attribute("Domain score", nominalPrediction);
        attributes.add(prediction);
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
            for (int poly = 0; poly < getPolynomsToAdd(); poly++) {
                double pow = Math.pow(instance.value(i), poly + 2);
                instance.setValue(attributeIndexStartPolys + offset + poly, pow);
            }
            offset += getPolynomsToAdd();
        }
        return instance;
    }


    /**
     * SQRT((SUM(tokens => if(token.length > 1)
     * tokens.length^2)/tokens.length)-avg^2)
     */
    protected double tokenLengthVariance(String[] tokens,
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

    protected double averageTokenLength(String[] tokens) {
        double avg = 0.0d;
        int scored = 0;
        for (String s : tokens) {
            avg += s.length();
            scored++;
        }
        return avg / scored;
    }

    protected int count(String sourceString, char lookFor) {
        int count = 0;
        for (int i = 0; i < sourceString.length(); i++) {
            final char c = sourceString.charAt(i);
            if (c == lookFor) {
                count++;
            }
        }
        return count;
    }

    protected int countVerbs(String[] taggedTokens) {
        int count = 0;
        for (String s : taggedTokens) {
            if (s.charAt(0) == 'V') {
                count++;
            }
        }
        return count;
    }

    protected int countAdjectives(String[] taggedTokens) {
        int count = 0;
        for (String s : taggedTokens) {
            if (s.charAt(0) == 'J') {
                count++;
            }
        }
        return count;
    }

    protected int countNouns(String[] taggedTokens) {
        int count = 0;
        for (String s : taggedTokens) {
            if (s.charAt(0) == 'N') {
                count++;
            }
        }
        return count;
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
