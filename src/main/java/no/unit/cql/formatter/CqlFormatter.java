package no.unit.cql.formatter;

import com.google.common.net.UrlEscapers;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;

public class CqlFormatter {

    private static final String BEGINS_COMPARATOR = "=";
    private static final String TERM_PATH_SEPARATOR = ".";
    private static final String TERM_SET = "alma";
    private static final String MODIFIER_SEPARATOR = "/";
    private static final String CLAUSE_SEPARATOR = " ";

    private static final String AUTHORITY_ID = "authority_id";
    private static final String CREATOR = "creator";
    private static final String PUBLICATION_DATE = "main_pub_date";


    private static final String SORT_BY = "sortBy";
    private static final String SORT_MODIFIER = "sort.descending";
    public static final int LAST_TWENTY_YEARS = 20;
    public static final String LOGICAL_AND = "AND";
    public static final String STRING_DELIMITER = "\"";
    public static final String CLAUSE_DELIMITER_LEFT = "(";
    public static final String CLAUSE_DELIMITER_RIGHT = ")";
    private static final CharSequence LOGICAL_OR = "OR";
    public static final String WHITESPACE = " ";

    private String authorityId;
    private String creator;
    private boolean sorted;
    private boolean retrospective;

    public CqlFormatter withAuthorityId(String authorityId) {
        this.authorityId = authorityId;
        return this;
    }

    public String format() {

        List<String> clauses = new ArrayList<>();

        if (nonNull(this.authorityId)) {
            clauses.add(generateCqlClause(generateIndex(AUTHORITY_ID), this.authorityId));
        }

        if (nonNull(this.creator)) {
            clauses.add(generateCqlClause(generateIndex(CREATOR), this.creator));
        }

        if (retrospective) {
            clauses.add(generateDateClause());
        }

        String query = String.join(CLAUSE_SEPARATOR + LOGICAL_AND + CLAUSE_SEPARATOR, clauses);

        if (sorted) {
            query = query + CLAUSE_SEPARATOR + SORT_BY + CLAUSE_SEPARATOR + generateSortSpecification(generateIndex(PUBLICATION_DATE));
        }

        return query;
    }

    private String generateSortSpecification(String index) {
        return index + MODIFIER_SEPARATOR + CqlFormatter.SORT_MODIFIER;
    }

    private String generateIndex(String index) {
        return String.join(TERM_PATH_SEPARATOR, CqlFormatter.TERM_SET, index);
    }

    public CqlFormatter withCreator(String creator) {
        this.creator = creator;
        return this;
    }

    private String generateDateClause() {
        int currentYear = Year.now().getValue();
        List<String> dateClauses = new ArrayList<>();

        IntStream.range(0, LAST_TWENTY_YEARS).forEach(year -> dateClauses.add(generateCqlClause(generateIndex(PUBLICATION_DATE), String.valueOf(currentYear - year))));
        Collections.reverse(dateClauses);

        return CLAUSE_DELIMITER_LEFT + String.join(String.join(LOGICAL_OR, CLAUSE_SEPARATOR, CLAUSE_SEPARATOR), dateClauses) + CLAUSE_DELIMITER_RIGHT;
    }

    private String generateCqlClause(String term, String value) {
        if (value.contains(WHITESPACE)) {
            value = STRING_DELIMITER + value + STRING_DELIMITER;
        }
        return String.join(CqlFormatter.BEGINS_COMPARATOR, term, value);
    }

    public CqlFormatter withRetrospective(boolean retrospective) {
        this.retrospective = retrospective;
        return this;
    }

    public CqlFormatter withSorting(boolean sorting) {
        this.sorted = sorting;
        return this;
    }

    public String encode() {
        return UrlEscapers.urlPathSegmentEscaper().escape(format());
    }
}
