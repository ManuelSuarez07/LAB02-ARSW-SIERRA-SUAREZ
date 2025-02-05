package snakepackage;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;
import enums.GridSize;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author jd-
 */
public class SnakeApp {

    private Snake winner;
    private Snake loser;
    private Object pauseObject;
    private JButton pause;
    private JButton start;
    private JButton resume;
    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    Snake[] snakes = new Snake[MAX_THREADS];
    private static final Cell[] spawn = {
        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
        new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2)
    };
    private JFrame frame;
    private static Board board;
    int nr_selected = 0;
    Thread[] thread = new Thread[MAX_THREADS];
    
    // New labels to display the longest and worst snake
    private JLabel longestSnakeLabel;
    private JLabel worstSnakeLabel;

    public SnakeApp() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 40);
        frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
                dimension.height / 2 - frame.getHeight() / 2);
        board = new Board();
        pause = new JButton("Pause");
        resume = new JButton("Resume");
        start = new JButton("Start");

        frame.add(board, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new FlowLayout());
        actionsPanel.add(pause);
        actionsPanel.add(resume);
        actionsPanel.add(start);
        
        // Initialize labels
        longestSnakeLabel = new JLabel("Longest Snake: ");
        worstSnakeLabel = new JLabel("Worst Snake: ");
        actionsPanel.add(longestSnakeLabel);
        actionsPanel.add(worstSnakeLabel);
        
        frame.add(actionsPanel, BorderLayout.SOUTH);
        addEvents();
    }

    public Snake getWinner() {
        return winner;
    }

    public void setWinner(Snake winner) {
        this.winner = winner;
    }

    public static void main(String[] args) {
        app = new SnakeApp();
        app.init();
    }

    private void init() {
        boolean loserSnake = true;
        pauseObject = new Object();
        loser = null;
        for (int i = 0; i != MAX_THREADS; i++) {
            snakes[i] = new Snake(i + 1, spawn[i], i + 1, pauseObject);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
        }

        frame.setVisible(true);
        while (true) {
            AtomicInteger x = new AtomicInteger(0);
            for (int i = 0; i != MAX_THREADS; i++) {
                if (snakes[i].isSnakeEnd()) {
                    x.getAndIncrement();
                    if (loserSnake) {
                        loser = snakes[i];
                        loserSnake = false;
                    }
                }
            }
            if (x.get() == MAX_THREADS) {
                break;
            }
        }

        System.out.println("Thread (snake) status:");
        for (int i = 0; i != MAX_THREADS; i++) {
            System.out.println("[" + i + "] :" + thread[i].getState());
        }
    }

    public static SnakeApp getApp() {
        return app;
    }

    private void addEvents() {
        pause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Pause();
            }
        });

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Start();
            }
        });

        resume.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Resume();
            }
        });
    }

    private void Pause() {
        for (Snake sn : snakes) {
            sn.stop();
        }
        findWinner();

        longestSnakeLabel.setText("Longest Snake: " + winner.getIdt());
        if (loser != null) {
            worstSnakeLabel.setText("Worst Snake: " + loser.getIdt());
        }
    }

    private void Start() {
        for (int i = 0; i != MAX_THREADS; i++) {
            thread[i].start();
        }
    }

    private void Resume() {
        for (Snake sn : snakes) {
            sn.start();
        }
    }

    private void findWinner() {
        Snake winnerPartial = snakes[0];
        int max = snakes[0].getBody().size();
        for (int i = 0; i < MAX_THREADS; i++) {
            if (snakes[i].getBody().size() > max) {
                winnerPartial = snakes[i];
                max = snakes[i].getBody().size(); // Update max to the new winner's size
            }
        }
        setWinner(winnerPartial);
    }
}