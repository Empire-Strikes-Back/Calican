(ns Calican.main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]
   [clojure.repl :as Wichita.repl]

   [Calican.seed]
   [Calican.baked_potatoes]
   [Calican.groats]
   [Calican.popcorn]
   [Calican.salt])
  (:import
   (javax.swing JFrame WindowConstants ImageIcon JPanel JScrollPane JTextArea BoxLayout JEditorPane ScrollPaneConstants SwingUtilities JDialog)
   (javax.swing JMenu JMenuItem JMenuBar KeyStroke JOptionPane JToolBar JButton JToggleButton)
   (javax.swing.border EmptyBorder)
   (java.awt Canvas Graphics Graphics2D Shape Color Polygon Dimension BasicStroke Toolkit Insets BorderLayout)
   (java.awt.event KeyListener KeyEvent MouseListener MouseEvent ActionListener ActionEvent)
   (java.awt.geom Ellipse2D Ellipse2D$Double Point2D$Double)
   (com.formdev.flatlaf FlatLaf FlatLightLaf)
   (com.formdev.flatlaf.extras FlatUIDefaultsInspector FlatDesktop FlatDesktop$QuitResponse FlatSVGIcon)
   (com.formdev.flatlaf.util SystemInfo)
   (java.util.function Consumer)
   (java.util ServiceLoader)
   (org.kordamp.ikonli Ikon)
   (org.kordamp.ikonli IkonProvider)
   (org.kordamp.ikonli.swing FontIcon)
   (org.kordamp.ikonli.codicons Codicons))
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defonce stateA (atom nil))
(def ^:dynamic ^JFrame jframe nil)
(def ^:dynamic ^Canvas canvas nil)
(def ^:dynamic ^JTextArea repl nil)
(def ^:dynamic ^JTextArea output nil)
(def ^:dynamic ^JEditorPane editor nil)
(def ^:dynamic ^JScrollPane output-scroll nil)
(def ^:dynamic ^Graphics2D graphics nil)
(defonce ns* (find-ns 'Calican.main))

(def ^:const energy-per-move 100)
(def ^:const canvas-width 1600)
(def ^:const canvas-height 1600)
(def ^:const tile-size 32)
(def ^:const jframe-title "wait, i do wear beskar")

(defn eval-form
  ([form]
   (eval-form form {}))
  ([form {:keys [:print-form?] :or {print-form? true} :as opts}]
   (let [string-writer (java.io.StringWriter.)
         result (binding [*ns* ns*
                          *out* string-writer]
                  (eval form))]
     (doto output
       (.append "=> "))
     (when print-form?
       (doto output
         (.append (str form))
         (.append "\n")))
     (doto output
       (.append (str string-writer))
       (.append (if (string? result) result (pr-str result)))
       (.append "\n"))

     (go
       (<! (timeout 10))
       (let [scrollbar (.getVerticalScrollBar output-scroll)]
         (.setValue scrollbar (.getMaximum scrollbar)))))))

(defn draw-word
  "draw word"
  []
  (.drawString graphics "word" 500 500))

(defn draw-line
  "draw line"
  []
  (.drawLine graphics  500 500 1000 1000))

(defn clear-canvas
  []
  (.clearRect graphics 0 0 (.getWidth canvas)  (.getHeight canvas)))

(defn clear
  []
  (.setText output ""))

(defn transmit
  "evaluate code in spe-editor-bike"
  []
  (-> (.getText editor) (clojure.string/trim) (clojure.string/trim-newline) (read-string) (eval-form)))

(defn print-fns
  []
  (go
    (let [fn-names (keys (ns-publics 'Calican.main))]
      (doseq [fn-name fn-names]
        (print (eval-form `(with-out-str (Wichita.repl/doc ~fn-name)) {:print-form? false}))))))

(defn create-jframe
  []
  (let [jframe (JFrame. jframe-title)
        content-pane (.getContentPane jframe)
        panel (JPanel.)
        layout (BoxLayout. panel BoxLayout/X_AXIS)
        code-panel (JPanel.)
        code-layout (BoxLayout. code-panel BoxLayout/Y_AXIS)
        canvas (Canvas.)
        repl (JTextArea. 1 80)
        output (JTextArea. 14 80)
        output-scroll (JScrollPane.)
        editor (JEditorPane.)
        editor-scroll (JScrollPane.)
        screenshotsMode? (Boolean/parseBoolean (System/getProperty "flatlaf.demo.screenshotsMode"))

        on-menubar-item (fn [f]
                          (reify ActionListener
                            (actionPerformed [_ event]
                              (SwingUtilities/invokeLater
                               (reify Runnable
                                 (run [_]
                                   (f _ event)))))))

        on-menu-item-show-dialog (on-menubar-item (fn [_ event] (JOptionPane/showMessageDialog jframe (.getActionCommand ^ActionEvent event) "menu bar item" JOptionPane/PLAIN_MESSAGE)))]

    (let [jmenubar (JMenuBar.)]
      (doto jmenubar
        (.add (doto (JMenu.)
                (.setText "file")
                (.setMnemonic \F)
                (.add (doto (JMenuItem.)
                        (.setText "new")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_N (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \U)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "exit")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Q (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \X)
                        (.addActionListener (on-menubar-item (fn [_ event]
                                                               (.dispose jframe))))))))

        (.add (doto (JMenu.)
                (.setText "edit")
                (.setMnemonic \E)
                (.add (doto (JMenuItem.)
                        (.setText "undo")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Z (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \U)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "redo")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Y (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \R)
                        (.addActionListener on-menu-item-show-dialog)))
                (.addSeparator)
                (.add (doto (JMenuItem.)
                        (.setText "cut")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_X (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \C)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "copy")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_C (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \O)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "paste")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_V (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \P)
                        (.addActionListener on-menu-item-show-dialog)))
                (.addSeparator)
                (.add (doto (JMenuItem.)
                        (.setText "delete")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_DELETE 0))
                        (.setMnemonic \D)
                        (.addActionListener on-menu-item-show-dialog))))))

      (.setJMenuBar jframe jmenubar))

    (FlatDesktop/setQuitHandler (reify Consumer
                                  (accept [_ response]
                                    (.performQuit ^FlatDesktop$QuitResponse response))
                                  (andThen [_ after] after)))

    (let [jtoolbar (JToolBar.)]
      (doto jtoolbar
        (.setMargin (Insets. 3 3 3 3))
        (.add (doto (JButton.)
                (.setToolTipText "new file")
                (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/NEW_FILE 32 Color/BLACK))))
        (.add (doto (JButton.)
                (.setToolTipText "open file")
                (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/FOLDER_OPENED 32 Color/BLACK))))
        (.add (doto (JButton.)
                (.setToolTipText "save")
                (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/SAVE 32 Color/BLACK))))
        (.add (doto (JButton.)
                (.setToolTipText "undo")
                (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/DISCARD 32 Color/BLACK))))
        (.add (doto (JButton.)
                (.setToolTipText "redo")
                (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/REDO 32 Color/BLACK))))
        (.addSeparator))

      (.add content-pane jtoolbar BorderLayout/NORTH))


    (let [editor-panel (JPanel.)]
      (.add content-pane editor-panel BorderLayout/WEST))

    (let [canvas-panel (JPanel.)]
      (.add content-pane canvas-panel BorderLayout/EAST))

    (when-let [url (Wichita.java.io/resource "icon.png")]
      (.setIconImage jframe (.getImage (ImageIcon. url))))

   

    #_(do
        (doto editor
          (.setBorder (EmptyBorder. #_top 0 #_left 0 #_bottom 0 #_right 20)))

        (doto editor-scroll
          (.setViewportView editor)
          (.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER)
          (.setPreferredSize (Dimension. 800 1300)))

        (doto output
          (.setEditable false))

        (doto output-scroll
          (.setViewportView output)
          (.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER))

        (doto repl
          (.addKeyListener (reify KeyListener
                             (keyPressed
                               [_ event]
                               (when (= (.getKeyCode ^KeyEvent event) KeyEvent/VK_ENTER)
                                 (.consume ^KeyEvent event)))
                             (keyReleased
                               [_ event]
                               (when (= (.getKeyCode ^KeyEvent event) KeyEvent/VK_ENTER)
                                 (-> (.getText repl) (clojure.string/trim) (clojure.string/trim-newline) (read-string) (eval-form))
                                 (.setText repl "")))
                             (keyTyped
                               [_ event]))))

        (doto code-panel
          (.setLayout code-layout)
          (.add editor-scroll)
          (.add output-scroll)
          (.add repl))

        (doto canvas
          (.setSize canvas-width canvas-height)
          (.addMouseListener (reify MouseListener
                               (mouseClicked
                                 [_ event]
                                 (println :coordinate [(.getX ^MouseEvent event) (.getY ^MouseEvent event)]))
                               (mouseEntered [_ event])
                               (mouseExited [_ event])
                               (mousePressed [_ event])
                               (mouseReleased [_ event]))))

        (doto panel
          (.setLayout layout)
          (.add code-panel)
          (.add canvas))

        (doto jframe
          (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
          (.setSize 2400 1600)
          (.setLocation 1300 200)
          (.add panel)
          (.setVisible true)))


    (alter-var-root #'Calican.main/jframe (constantly jframe))
    (alter-var-root #'Calican.main/canvas (constantly canvas))
    (alter-var-root #'Calican.main/output-scroll (constantly output-scroll))
    (alter-var-root #'Calican.main/repl (constantly repl))
    (alter-var-root #'Calican.main/output (constantly output))
    (alter-var-root #'Calican.main/editor (constantly editor))
    (alter-var-root #'Calican.main/graphics (constantly (.getGraphics canvas)))

    #_(add-watch stateA :watch-fn
                 (fn [ref wathc-key old-state new-state]

                   (clear-canvas)
                   (.setPaint graphics (Color. 237 211 175 200))
                   (.fillRect graphics 0 0 (.getWidth canvas) (.getHeight canvas))))

    #_(go
        (<! (timeout 100))
        (eval-form `(print-fns)))
    nil))

(defn window
  []
  (let []

    (when SystemInfo/isMacOS
      (System/setProperty "apple.laf.useScreenMenuBar" "true")
      (System/setProperty "apple.awt.application.name" jframe-title)
      (System/setProperty "apple.awt.application.appearance" "system"))

    (when SystemInfo/isLinux
      (JFrame/setDefaultLookAndFeelDecorated true)
      (JDialog/setDefaultLookAndFeelDecorated true))

    (when (and
           (not SystemInfo/isJava_9_orLater)
           (= (System/getProperty "flatlaf.uiScale") nil))
      (System/setProperty "flatlaf.uiScale" "2x"))

    (SwingUtilities/invokeLater
     (reify Runnable
       (run [_]

         (FlatLightLaf/setup)

         (create-jframe)

         (.setPreferredSize jframe
                            (if SystemInfo/isJava_9_orLater
                              (Dimension. 830 440)
                              (Dimension. 1660 880)))

         #_(doto jframe
             (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
             (.setSize 2400 1600)
             (.setLocation 1300 200)
             #_(.add panel)
             (.setVisible true))
         (doto jframe
           (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
           (.pack)
           (.setLocationRelativeTo nil)
           (.setVisible true)))))))

(defn reload
  []
  (require '[Calican.main] :reload))

(defn process
  []
  (go
    (<! (timeout 1000))
    (println "Kuiil has spoken")))

(defn -main
  [& args]
  (println "i dont want my next job")
  (reset! stateA {})
  (window)
  (println "Kuiil has spoken"))