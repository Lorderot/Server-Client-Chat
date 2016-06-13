package view;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatConfigurations {
    private Color privateMessageColor = Color.FUCHSIA;
    private Color publicMessageColor = Color.BLACK;
    private Color serverMessageColor = Color.RED;
    private Font messageFont = Font.font("Times New Roman", 14);
    private Color chatBackgroundColor = Color.WHITE;
    private int smileWidth = 30;
    private int smileHeight = 30;
    private int messageLimit = 500;
    private char metaSymbolDelimiter = '|';
    private List<Character> metaSymbolsUsedInSmiles;
    private HashMap<String, String> smileys;

    public ChatConfigurations() {
        smileys = new HashMap<>();
        smileys.put("(anger)", "/view/Smiles/anger.gif");
        smileys.put("(applause)", "/view/Smiles/applause.gif");
        smileys.put("(cocktail)", "/view/Smiles/cocktail.gif");
        smileys.put("(cry)", "/view/Smiles/cry.gif");
        smileys.put("(crying)", "/view/Smiles/crying.gif");
        smileys.put("(cute)", "/view/Smiles/cute.gif");
        smileys.put("(donttalk)", "/view/Smiles/dont talk.gif");
        smileys.put("(giggle", "/view/Smiles/giggle.gif");
        smileys.put("(hi)", "/view/Smiles/hi.gif");
        smileys.put("(high five)", "/view/Smiles/high five.gif");
        smileys.put("(kiss)", "/view/Smiles/kiss.gif");
        smileys.put("(laugh)", "/view/Smiles/laugh.gif");
        smileys.put("(party)", "/view/Smiles/party.gif");
        smileys.put("(pray)", "/view/Smiles/pray.gif");
        smileys.put("(wink)", "/view/Smiles/wink.gif");
        metaSymbolsUsedInSmiles = new ArrayList<>();
        metaSymbolsUsedInSmiles.add('(');
        metaSymbolsUsedInSmiles.add(')');
    }

    public Color getPrivateMessageColor() {
        return privateMessageColor;
    }

    public void setPrivateMessageColor(Color privateMessageColor) {
        this.privateMessageColor = privateMessageColor;
    }

    public Color getPublicMessageColor() {
        return publicMessageColor;
    }

    public void setPublicMessageColor(Color publicMessageColor) {
        this.publicMessageColor = publicMessageColor;
    }

    public Color getServerMessageColor() {
        return serverMessageColor;
    }

    public void setServerMessageColor(Color serverMessageColor) {
        this.serverMessageColor = serverMessageColor;
    }

    public Font getMessageFont() {
        return messageFont;
    }

    public void setMessageFont(Font messageFont) {
        this.messageFont = messageFont;
    }

    public Color getChatBackgroundColor() {
        return chatBackgroundColor;
    }

    public void setChatBackgroundColor(Color chatBackgroundColor) {
        this.chatBackgroundColor = chatBackgroundColor;
    }

    public int getMessageLimit() {
        return messageLimit;
    }

    public void setMessageLimit(int messageLimit) {
        this.messageLimit = messageLimit;
    }

    public HashMap<String, String> getSmileys() {
        return smileys;
    }

    public char getMetaSymbolDelimiter() {
        return metaSymbolDelimiter;
    }

    public int getSmileWidth() {
        return smileWidth;
    }

    public int getSmileHeight() {
        return smileHeight;
    }

    public List<Character> getMetaSymbolsUsedInSmiles() {
        return metaSymbolsUsedInSmiles;
    }
}
