package searchengine.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.lemma.LemmaConverter;
import searchengine.entity.LemmaEntity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SnippetCreation {

    private final LemmaConverter lemmaConverter;

    private static final int SNIPPET_MAX_SIZE = 280;

    private static final String LETTERS_REGEX = "[^a-zа-я]";

    public String getSnippetFromPageContent(String content, Set<LemmaEntity> lemmas) {
        String snippetSketch = "";
        StringBuilder snippet = new StringBuilder();
        String[] words = lemmaConverter.splitTextToWords(content);

        Set<String> uniqueSetWords = new HashSet<>();
        for (LemmaEntity lemmaEntity : lemmas) {
            for (String word : words) {
                List<String> wordBaseForms = lemmaConverter.returnWordIntoBaseForm(word);
                if (wordBaseForms.isEmpty()) {
                    continue;
                }

                String resultWordForm = wordBaseForms.get(wordBaseForms.size() - 1);
                if (lemmaEntity.getLemma().equals(resultWordForm) && !uniqueSetWords.contains(word)) {
                    uniqueSetWords.add(word);
                    snippetSketch = findFragmentsWithQueryWord(snippet, word, content.toLowerCase()).toString();
                }
            }
        }

        return checkForSnippetSize(snippetSketch, content, uniqueSetWords);
    }

    private StringBuilder findFragmentsWithQueryWord(StringBuilder snippet, String word, String content) {
        if (!snippet.toString().contains(word)) {
            Pattern pattern = Pattern.compile("\\b.{0,20}" + LETTERS_REGEX + word + LETTERS_REGEX + ".{0,70}\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String fragmentText = content.substring(matcher.start(), matcher.end());
                String text = "..." + fragmentText;
                if (!snippet.toString().contains(fragmentText)) snippet.append(text);
            }
        }
        return snippet;
    }

    private String checkForSnippetSize(String snippet, String content, Set<String> uniqueSetWords) {
        StringBuilder builder = new StringBuilder(snippet);

        int len = snippet.length();
        if(len < SNIPPET_MAX_SIZE) {
            int symbolsRemaining = SNIPPET_MAX_SIZE - len;
            int indexSnippet = snippet.lastIndexOf("...") + 3;
            int indexContent = indexSnippet + symbolsRemaining;
            String extraSymbols = content.indexOf(indexContent) == -1 ? content.toLowerCase().substring(0, indexContent) : content.substring(indexSnippet, indexContent);
            builder.append("...").append(extraSymbols).append("...");
        } else {
            builder.replace(SNIPPET_MAX_SIZE + 1, builder.length(), "");
        }
        return addTagsToWordInSnippet(builder.toString(), uniqueSetWords);
    }

    private String addTagsToWordInSnippet(String snippet, Set<String> uniqueSetWords) {
        for(String word : uniqueSetWords) {
            String regex = LETTERS_REGEX + word + LETTERS_REGEX;
            snippet = snippet.replaceAll(regex, " <b>" + word + "</b> ");
        }

        return snippet.endsWith("...") ? snippet : snippet.concat("...");
    }
}
