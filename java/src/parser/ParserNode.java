package parser;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;


@Getter
class ParserNode {
    private String prop;
    List<ParserNode> children;
    int val;

    public ParserNode(int val) {
        this.val = val;
    }

    public ParserNode(String s) {
        this.prop = s;
        this.children = new LinkedList<>();

        if (prop.charAt(0) == '{') {
            while (prop.length() != 0) {
                consumeOneArray();
                removeComas();
            }

        } else {
            Matcher matcher = compile("-?[0-9]+").matcher(prop);
            while (matcher.find()) children.add(new ParserNode(parseInt(matcher.group())));
            prop = "";
        }
    }

    public Stream<ParserNode> streamChildren() {
        return getChildren().stream();
    }

    private void consumeOneArray() {
        int nbOpen = 1;
        int ind = 1;
        char c;

        while (nbOpen != 0) {
            c = prop.charAt(ind);

            if (c == '{') {
                nbOpen++;
            } else if (c == '}') {
                nbOpen--;
            }
            ind++;
        }

        this.children.add(new ParserNode(prop.substring(1, ind - 1)));
        prop = prop.substring(ind);
    }

    private void removeComas() {
        if (prop.length() != 0) {
            if (prop.charAt(0) == ',') {
                prop = prop.substring(1);
            }
        }
    }
}
