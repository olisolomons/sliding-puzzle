(ns sliding-puzzle.game
  (:require
   [quil.core :as q]
   [sliding-puzzle.easing-functions :as easing-functions]
   [sliding-puzzle.sketch-functions :refer [add-derived-state draw-state
                                            key-pressed mouse-dragged mouse-pressed
                                            mouse-released update-state]]
   [sliding-puzzle.win-screen :as win-screen]))

(defn mk-state [canvas-size size pieces won? goal-text next-level]
  (add-derived-state
   {:type :game
    :size size
    :canvas-size canvas-size
    :pieces (into {} (map vector pieces (range)))
    :id->pos (into {} (map vector (range) pieces))
    :won? won?
    :goal-text goal-text
    :next-level next-level}))

(def margin 0.08)

(def animation-duration-ms 400)

(defn draw-piece [{:keys [box-width selected mouse-pressed?]} id x y]
  (if (and (= id selected) (not mouse-pressed?))
    (q/fill 80 60 200 200)
    (q/fill 80 30 240 200))
  (q/ellipse (* (+ x 0.5) box-width) (* (+ 0.5 y) box-width)
             (- box-width 20) (- box-width 20)))

(defn vector-lerp [[x1 y1] [x2 y2] amount]
  [(q/lerp x1 x2 amount) (q/lerp y1 y2 amount)])

(defmethod draw-state :game
  [{:keys [size pieces animations box-width goal-text canvas-size] :as state}]
  (q/no-stroke)
  (q/fill 50)
  (q/text-size (* canvas-size 0.05))
  (q/text-align :center :center)
  (q/text goal-text (* canvas-size 0.5) (* canvas-size margin 0.5))

  (q/no-fill)
  (q/stroke 60)
  (q/stroke-weight (max (* canvas-size 0.01) 1))
  (q/stroke-join :round)
  (q/with-translation
    [(* margin canvas-size) (* margin canvas-size)]
    (let [width (* box-width size)]
      (q/rect 0 0 width width)
      (doseq [i (range (inc size))]
        (q/line (* box-width i) 0 (* box-width i) width))
      (doseq [i (range (inc size))]
        (q/line 0 (* box-width i) width (* box-width i))))

    (doseq [[[x y] id] pieces]
      (if-let [{:keys [from to start-time end-time]}
               (get animations id)]
        (apply draw-piece state id
               (vector-lerp from to
                            (easing-functions/ease-in-out-cubic
                             (/ (- (q/millis) start-time)
                                (- end-time start-time)))))
        (draw-piece state id x y)))))

(defmethod update-state :game
  [{:keys [won?] :as state}]
  (let [t (q/millis)
        finished-animations
        (into []
              (comp
               (filter (comp #(> t %) :end-time val))
               (map key))
              (:animations state))]
    (if (seq finished-animations)
      (let [state' (update state :animations #(apply dissoc % finished-animations))]
        (if (and (empty? (:animations state'))
                 (won? state'))
          (win-screen/mk-state state')
          state'))
      state)))

(defn to-coords [{:keys [box-width canvas-size]} pos]
  (map #(int (/ (- % (* margin canvas-size)) box-width)) pos))

(defmethod mouse-pressed :game
  [{:keys [pieces] :as state} {:keys [x y button]}]
  (if (= button :left)
    (let [coords (to-coords state [x y])]
      (assoc state
             :selected (get pieces coords)
             :mouse-pressed? true))
    state))

(defmethod mouse-released :game
 [state _event]
 (dissoc state :mouse-pressed?))

(defn vec+ [& vectors]
  (apply mapv + vectors))

(defn vec- [& vectors]
  (apply mapv - vectors))

(defn magnitude-squared [[x y]]
  (+ (* x x) (* y y)))

(defn in-bounds? [{:keys [size]} pos]
  (every? #(<= 0 % (dec size)) pos))

(defn move [{:keys [selected] :as state} selected-pos direction]
  (let [next-pos (vec+ selected-pos direction)]
    (-> state
        (update :pieces dissoc selected-pos)
        (assoc-in [:pieces next-pos] selected)
        (assoc-in [:id->pos selected] next-pos))))

(defn slide [state selected-pos direction]
  (let [original-pos selected-pos]
    (loop [{:keys [pieces selected] :as state} state
           selected-pos selected-pos]
      (let [next-pos (vec+ selected-pos direction)]
        (if (and (in-bounds? state next-pos) (not (get pieces next-pos)))
          (recur (move state selected-pos direction) next-pos)
          (assoc-in state
                    [:animations selected]
                    {:from original-pos
                     :to selected-pos
                     :start-time (q/millis)
                     :end-time (+ (q/millis) animation-duration-ms)}))))))

(defmethod mouse-dragged :game
  [{:keys [id->pos selected] :as state} {:keys [x y button]}]
  (or (when (= button :left)
        (when-let [selected-pos (get id->pos selected)]
          (let [current (to-coords state [x y])
                direction (vec- current selected-pos)]
            (when (= (magnitude-squared direction) 1)
              (-> state
                  (slide selected-pos direction)
                  (dissoc :selected))))))
      state))

(defmethod key-pressed :game
  [{:keys [selected id->pos] :as state} event]
  (if-let [selected-pos (get id->pos selected)]
    (case (:key event)
      (:ArrowRight :d) (slide state selected-pos [1 0])
      (:ArrowLeft :a) (slide state selected-pos [-1 0])
      (:ArrowUp :w) (slide state selected-pos [0 -1])
      (:ArrowDown :s) (slide state selected-pos [0 1])
      :Tab (assoc state :selected
                  (if (get id->pos (inc selected))
                    (inc selected)
                    0))
      state)
    (if (= (:key event) :Tab)
      (assoc state :selected 0)
      state)))

(defmethod add-derived-state :game
  [{:keys [canvas-size size] :as state}]
  (assoc state
         :box-width
         (/ (- canvas-size (* 2 margin canvas-size)) size)))

