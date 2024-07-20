package searchengine.lemma;

import jakarta.annotation.PostConstruct;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;


@Component
public class LemmaConverter {

    private static final String REGEX = "[^а-яА-Яa-zA-Z\\s]";

    private static final String[] RUS_FUNCTIONAL_TYPES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ"};

    private static final String[] ENG_FUNCTIONAL_TYPES = new String[]{"CONJ", "PREP", "ARTICLE", "PART", "INT"};

    private RussianLuceneMorphology russianLuceneMorphology;

    private EnglishLuceneMorphology englishLuceneMorphology;

    @PostConstruct
    public void init() throws IOException {
        russianLuceneMorphology = new RussianLuceneMorphology();
        englishLuceneMorphology = new EnglishLuceneMorphology();
    }

    public Map<String, Integer> convertContentToLemmas(String content) {
        String text = Jsoup.parse(content).text();
        String[] words = splitTextToWords(text);
        HashMap<String, Integer> lemmas = new HashMap<>();
        for (String word : words) {
            List<String> wordBaseForms = returnWordIntoBaseForm(word);
            if (wordBaseForms.isEmpty()) {
                continue;
            }

            String lemma = wordBaseForms.get(wordBaseForms.size() - 1);
            if (!lemmas.containsKey(lemma)) {
                lemmas.put(lemma, 1);
            } else {
                int frequency = lemmas.get(lemma) + 1;
                lemmas.put(lemma, frequency);
            }
        }
        return lemmas;
    }

    public String[] splitTextToWords(String text) {
        String[] words;

        if (text.substring(0, 1).matches(REGEX)) {
            words = text.toLowerCase(Locale.ROOT).replace(text.substring(0, 1), "")
                    .replaceAll(REGEX, " ").split("\\s+");
        } else {
            words = text.toLowerCase(Locale.ROOT).replaceAll(REGEX, " ").split("\\s+");
        }

        return words;
    }

    public List<String> returnWordIntoBaseForm(String word) {
        List<String> lemmaList = new ArrayList<>();
        if (checkLanguage(word).name().equals("RUSSIAN") && (!word.isEmpty() && isRusWordFunctional(word))) {
            List<String> baseRusForm = russianLuceneMorphology.getNormalForms(word);
            lemmaList.add(baseRusForm.get(baseRusForm.size() - 1));
        } else if (checkLanguage(word).name().equals("ENGLISH") && (!word.isEmpty() && !isEngWordFunctional(word))) {
            List<String> baseEngForm = englishLuceneMorphology.getNormalForms(word);
            lemmaList.add(baseEngForm.get(baseEngForm.size() - 1));
        }
        return lemmaList;
    }

    private Languages checkLanguage(String word) {
        String russianAlphabet = "[а-яА-Я]{2,}";
        String englishAlphabet = "[a-zA-Z]{2,}";

        if (word.matches(russianAlphabet)) {
            return Languages.RUSSIAN;
        } else if (word.matches(englishAlphabet)) {
            return Languages.ENGLISH;
        } else {
            return Languages.NONEXISTENT;
        }
    }

    private boolean isRusWordFunctional(String word) {
        List<String> morphForm = russianLuceneMorphology.getMorphInfo(word);
        boolean result = false;
        for (String functionalType : RUS_FUNCTIONAL_TYPES) {
            if (morphForm.get(morphForm.size() - 1).contains(functionalType) || word.length() < 3) {
                result = true;
                break;
            }
        }
        return !result;
    }

    private boolean isEngWordFunctional(String word) {
        List<String> morphForm = englishLuceneMorphology.getMorphInfo(word);
        boolean result = false;
        for (String functionalType : ENG_FUNCTIONAL_TYPES) {
            if (morphForm.get(morphForm.size() - 1).contains(functionalType) || word.length() <= 3) {
                result = true;
                break;
            }
        }
        return result;
    }

    public StringBuilder editUrl(String url) {
        StringBuilder editedSite = new StringBuilder(url);
        if (url.contains("www.")) {
            int start = url.indexOf("www");
            int end = start + 4;
            editedSite.replace(start, end, "");
        }
        return editedSite;
    }
}