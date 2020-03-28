package ch.epfl.javass.gui;

import ch.epfl.javass.jass.PlayerId;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ChatBean {
	private StringProperty chatProperty;
	private StringProperty received;
	private StringProperty sent;

	public ChatBean() {
		chatProperty = new SimpleStringProperty("");
		received = new SimpleStringProperty("");
		sent = new SimpleStringProperty("");
	}

	public ReadOnlyStringProperty getChatProperty() {
		return chatProperty;
	}
	
	public ReadOnlyStringProperty getRecieved() {
		return received;
	}
	
	public ReadOnlyStringProperty getSent() {
		return sent;
	}
	
	public void addMessage(String s) {
		chatProperty.set(chatProperty.get() + s + "\n\n");
	}
	
	public void setReceived(String s) {
		received.set(s);
	}
	
	public void setSent(String s) {
		sent.set(s);
	}
}