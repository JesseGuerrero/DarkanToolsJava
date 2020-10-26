import libs.DarkanTools;


////import org.json.simple.parser.JSONParser;
//import libs.DarkanTools;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

//TODO: Create function for receiving sell offers, tag each with a found date
//TODO: Create a GE database
//TODO: Add a GE_Offers Table with columns: PLAYER(Primary Key), ITEM, AMOUNT, PRICE, DATE
//TODO: Think of a way to use databases to amplify Darkan Analysis
//TODO: Make dark theme
//TODO: Create a DB with Java -> SQLite3
//TODO: Player XP, HS

//setup db pathing
//static String urlDB = "jdbc:sqlite:" + (new File("").getAbsolutePath())  + "\\database.db";



public class Viewer {
    public static class XButton implements Runnable {
        private JInternalFrame firstWin;

        public XButton(JInternalFrame frame) {
            this.firstWin = frame;
        }

        public void run() {
            while(true) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    ;
                }
                if(firstWin.isClosed()) {
                    System.exit(0);
                }
            }
        }
    }

    //Setup interactable as static and open for methods to manipulate. Open to manipulate becasue they are outside of main
    static JButton buyButton, sellButton, nextPage, prevPage;
    static JPanel geOffers;
    static JSONArray sellOffers, buyOffers;
    static boolean isBuy;
    static int page;

    public static void updateGEOffer() {
        JSONArray offers;



        //Api Req
        if(isBuy) {//buy offer
            offers = buyOffers;
            buyButton.setText("----------");
            sellButton.setText("Sell Offer");
        } else if(isBuy == false) {//selloffer
            offers = sellOffers;
            buyButton.setText("Buy Offer");
            sellButton.setText("----------");
        } else {//null or other
            return;
        }

        int offerSize = offers.size();

        //Control prevButton view
        if(page == 0) {
            prevPage.setText("----------");
        } else {
            prevPage.setText("<--Prev");
        }

        // is our end range is bigger than the offers
        int index = page*20;
        if (index >= offerSize) {
            nextPage.setText("---------");;
        } else {//if its not then remove and repopulate. Otherwise stick to last time
            nextPage.setText("Next-->");

            geOffers.removeAll();
            geOffers.revalidate();
            geOffers.repaint();
            for (int i = index; i < index + 20; i++) {
                if(i >= offerSize) {
                    //System.out.println("i: " + i + " size: " + offerSize);
                    nextPage.setText("----------");
                    break;
                }
                String player, item, cost, itemID; //TODO: Add date objects
                //player = ((JSONObject)offers.get(i)).get("owner").toString();
                itemID = ((JSONObject) offers.get(i)).get("itemId").toString();
                item = ((JSONObject) offers.get(i)).get("itemName").toString();
                int amt = Integer.parseInt(((JSONObject) offers.get(i)).get("amountLeft").toString());
                int price = Integer.parseInt(((JSONObject) offers.get(i)).get("pricePerItem").toString());

                cost = String.valueOf(amt * price);


                InputStream is = Viewer.class.getResourceAsStream("/GE_Icons/"+itemID+".gif");

                try {
                    Image image = ImageIO.read(is);
                    ImageIcon gif = new ImageIcon(image);
                    JLabel offer = new JLabel("[" + (i + 1) + "]" + item + "x" + amt + " " + cost + "GP", gif, SwingConstants.HORIZONTAL);
                    geOffers.add(offer);
                    offer.setHorizontalAlignment(SwingConstants.LEFT);
                    is.close();
                } catch(Exception e) { ; }

            }
        }


    }

    public static void main(String[] args) throws Exception {
        //System.out.println(new File(".").getAbsolutePath());
        String apiSellResponse = DarkanTools.getAPIReq("https://darkan.org/api/ge/sell");
        String apiBuyResponse = DarkanTools.getAPIReq("https://darkan.org/api/ge/buy");
        JSONParser parse = new JSONParser();
        sellOffers = (JSONArray) parse.parse(apiSellResponse);
        buyOffers = (JSONArray) parse.parse(apiBuyResponse);


//        createNewTable(urlDB);
        JFrame frame = new JFrame();

        //Internal Frame
        JInternalFrame geViewer = new JInternalFrame();
        geViewer.setTitle("Darkan GE Viewer");
        geViewer.setClosable(true);
        geViewer.setVisible(true);

        InputStream is = Viewer.class.getResourceAsStream("/gold.png");
        Image image = ImageIO.read(is);
        geViewer.setFrameIcon(new ImageIcon(image));
        is.close();



        //Setup outmost frame to have no border
        frame.add(geViewer);
        frame.setUndecorated(true);
        frame.setSize(600, 425);
        frame.setVisible(true);

        //if Internal frame closes, exit JVM
        XButton emp = new XButton(geViewer);
        Thread checkWindows = new Thread(emp);
        checkWindows.start();

        /*
        *   Start adding components
        *   Each page processes 16 GE offers. It is organized by number. The max available is size of API Request.
        *   The prev, next button becomes transparent when first and last numbers are reached
        *   GE Offers contains both buy/sell offers
         */

        //GE Offers components
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        geViewer.add(panel);


        geOffers = new JPanel(new GridLayout(10,2));
        JPanel buttons = new JPanel(new FlowLayout());

        gbc.gridy = 0;
        gbc.ipadx = 450;
        gbc.ipady = 450;
        panel.add(geOffers, gbc);

        gbc.gridy = 1;
        gbc.ipadx = 250;
        panel.add(buttons, gbc);


        buyButton = new JButton("Buy Offer");
        sellButton = new JButton("Sell Offer");
        prevPage = new JButton("<--Prev");
        nextPage = new JButton("Next-->");

        buttons.add(buyButton);
        buttons.add(sellButton);
        buttons.add(prevPage);
        buttons.add(nextPage);

        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isBuy = true;
                page = 0;

                updateGEOffer();
            }
        });

        sellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isBuy = false;
                page = 0;

                updateGEOffer();
            }
        });

        nextPage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                page++;
                updateGEOffer();
            }
        });

        prevPage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                page--;
                updateGEOffer();
            }
        });

    }


}
