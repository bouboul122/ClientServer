public interface Layer {

    public void sendToLowerLayer();
    public void getFromLowerLayer();
    public void sendToHigherLayer();
    public void getFromHigherLayer();

}
