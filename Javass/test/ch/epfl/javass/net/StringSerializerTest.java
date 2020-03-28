package ch.epfl.javass.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Trick;

class StringSerializerTest {

    @Test
    void serializeIntSeemsGreat() {
        Trick trick = Trick.firstEmpty(Color.HEART, PlayerId.PLAYER_3);
        assertEquals("60ffffff", StringSerializer.serializeInt(trick.packed()));
        trick = trick.withAddedCard(Card.ofPacked(0b010010));
        assertEquals("60ffffd2", StringSerializer.serializeInt(trick.packed()));
        trick = trick.withAddedCard(Card.ofPacked(0b010001));
        assertEquals("60fff452", StringSerializer.serializeInt(trick.packed()));
        trick = trick.withAddedCard(Card.ofPacked(0b010100));
        assertEquals("60fd4452", StringSerializer.serializeInt(trick.packed()));
        trick = trick.withAddedCard(Card.ofPacked(0b010101));
        assertEquals("60554452", StringSerializer.serializeInt(trick.packed()));
    }

    @Test
    void deserializeIntSeemsGreat() {
        Trick trick = Trick.firstEmpty(Color.HEART, PlayerId.PLAYER_3);
        assertEquals(trick.packed(),
                StringSerializer.deserializeInt("60ffffff"));
        trick = trick.withAddedCard(Card.ofPacked(0b010010));
        assertEquals(trick.packed(),
                StringSerializer.deserializeInt("60ffffd2"));
        trick = trick.withAddedCard(Card.ofPacked(0b010001));
        assertEquals(trick.packed(),
                StringSerializer.deserializeInt("60fff452"));
        trick = trick.withAddedCard(Card.ofPacked(0b010100));
        assertEquals(trick.packed(),
                StringSerializer.deserializeInt("60fd4452"));
        trick = trick.withAddedCard(Card.ofPacked(0b010101));
        assertEquals(trick.packed(),
                StringSerializer.deserializeInt("60554452"));

    }

    @Test
    void serializeLongSeemsGreat() {
        CardSet hand = CardSet.of(Arrays.asList(Card.of(Color.SPADE, Rank.NINE),
                Card.of(Color.SPADE, Rank.KING), Card.of(Color.HEART, Rank.SIX),
                Card.of(Color.HEART, Rank.EIGHT),
                Card.of(Color.HEART, Rank.NINE),
                Card.of(Color.HEART, Rank.KING),
                Card.of(Color.DIAMOND, Rank.SEVEN),
                Card.of(Color.CLUB, Rank.SIX), Card.of(Color.CLUB, Rank.JACK)));
        assertEquals("210002008d0088",
                StringSerializer.serializeLong(hand.packed()));
    }

    @Test
    void deserializeLongSeemsGreat() {
        CardSet hand = CardSet.of(Arrays.asList(Card.of(Color.SPADE, Rank.NINE),
                Card.of(Color.SPADE, Rank.KING), Card.of(Color.HEART, Rank.SIX),
                Card.of(Color.HEART, Rank.EIGHT),
                Card.of(Color.HEART, Rank.NINE),
                Card.of(Color.HEART, Rank.KING),
                Card.of(Color.DIAMOND, Rank.SEVEN),
                Card.of(Color.CLUB, Rank.SIX), Card.of(Color.CLUB, Rank.JACK)));
        assertEquals(hand.packed(),
                StringSerializer.deserializeLong("210002008d0088"));
    }

    @Test
    void serializeStringSeemsGreat() {
        assertEquals("QW3DqWxpZQ==",
                StringSerializer.serializeString("Amélie"));
        assertEquals("R2HDq2xsZQ==",
                StringSerializer.serializeString("Gaëlle"));
        assertEquals("w4ltaWxl", StringSerializer.serializeString("Émile"));
        assertEquals("TmFkw6hnZQ==",
                StringSerializer.serializeString("Nadège"));
    }

    @Test
    void deserializeStringSeemsGreat() {
        assertEquals("Amélie",
                StringSerializer.deserializeString("QW3DqWxpZQ=="));
        assertEquals("Gaëlle",
                StringSerializer.deserializeString("R2HDq2xsZQ=="));
        assertEquals("Émile", StringSerializer.deserializeString("w4ltaWxl"));
        assertEquals("Nadège",
                StringSerializer.deserializeString("TmFkw6hnZQ=="));
    }

    @Test
    void combineSeemsGreat() {
        String[] turnState = {"0", "1ff01ff01ff01ff", "60ffffff"};
        assertEquals("0,1ff01ff01ff01ff,60ffffff", StringSerializer.combine(',', turnState));
    }

    @Test
    void splitSeemsGreat() {
        String[] turnState = {"0", "1ff01ff01ff01ff", "60ffffff"};
        assertArrayEquals(turnState,
                StringSerializer.split(',', "0,1ff01ff01ff01ff,60ffffff"));
    }

}
