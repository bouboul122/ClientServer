public interface Layer {
    public void getFromHigherLayer();
    public void sendToLowerLayer();
    public void getFromLowerLayer();
    public void sendToHigherLayer();
}
