package extra;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * It creates a client of type two. This kind of client is interested in Google, Roche and Repsol.
 * Every two days he buys two shares. This class extends from extra.Client
 *
 * @author Sergio Sánchez-Carvajales Francoy, Aarón Cabero Blanco, Juan Diego Valencia Marin, Daniel Tomas Sanchez
 */

public class ClientType2 extends Client {
    private int dia;

    /**
     * Creates a client of type two with the same attributes as extra.Client
     */
    public ClientType2() {
        super();
        dia = 1;
    }

    /**
     * It buys the shares that the second client wants to buy
     *
     * @author Aarón Cabero Blanco
     */
    protected void makePurchase() {
        Enterprise enterprise;
        String ticker;
        float cost;
        ArrayList<News> tickerNews;
        for (Quotation quotation : quotations) {
            enterprise = quotation.getEnterprise();
            ticker = enterprise.getTicker();
            cost = quotation.getCost();
            if (dia % 2 == 0)
                updatePrice(ticker, cost);
            else if (ticker.equals("GOOGL") || ticker.equals("RO") || ticker.equals("REP")) {
                tickerNews = news.get(ticker);
                updatePrice(ticker, quotation.getCost());
                if (tickerNews == null) continue;
                int goodNews = 0;
                for (News dailyNew : tickerNews) {
                    if (getOpinion(dailyNew.getContent()))
                        goodNews++;
                }
                if (goodNews >= 2)
                    buyShare(ticker, 2, cost);
            }
        }
        dia++;
        this.quotations = null;
        this.news = null;
    }

    /**
     * Buys two actions each two days from enterprises as GOOGL, REP y RO
     *
     * @param message The message that the client has receive from the extra.StockBroker.
     * @author Aarón Cabero Blanco
     */
    @SuppressWarnings("unchecked")
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {
            ObjectMessage objMessage = (ObjectMessage) message;
            Object obj = null;
            try {
                obj = objMessage.getObject();
            } catch (JMSException e) {
                System.out.println("Información inválida");
            }

            if (obj != null) {
                try {
                    if (obj instanceof ArrayList) {
                        this.quotations = (ArrayList<Quotation>) obj;
                    } else if (obj instanceof HashMap) {
                        this.news = (HashMap<String, ArrayList<News>>) obj;
                    }

                    if (this.quotations != null && this.news != null) {
                        makePurchase();
                    }
                } catch (Exception e) {
                    System.out.println("Error en el tratamiento de la información");
                }
            } else {
                this.finish();
            }
        }
    }

    public static void main(String[] args) {
        Client client2 = new ClientType2();
        try {
            client2.startConnection();
        } catch (JMSException jmse) {
            System.out.println("Error al establecer la conexión");
        }

        while (!client2.isFinished()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("Error al esperar la conexión");
            }
        }

        try {
            client2.closeConnection();
        } catch (JMSException jmse) {
            System.out.println("Error al cerrar la conexión");
        }

        System.out.println("Cliente 2:");
        client2.printInvestment();

    }

}
