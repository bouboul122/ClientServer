public interface Layer {

    void sendToLowerLayer();
    void getFromLowerLayer();
    void sendToHigherLayer();
    void getFromHigherLayer();

}
