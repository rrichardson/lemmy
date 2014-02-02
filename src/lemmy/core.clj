(ns lemmy.core
  (use [clojure.string :only [join split]]
       [clojure.core.match :only [match]])
  (:gen-class))


;; make two groups in a parsing regex, the first is made up of either the numbers 2-10 or J,Q,K or A
;; The second group is one of h, s, d or c
;; The search is constrained to match both the beginning and end of the string,
;; meaning that the max allowable search string is length 3
;; A grouped query in clojure returns a vector, the 0th entry is the total matched string,
;; which we discard. We then utilize the value of the card and the suit in the next sanity
;; check, which is pretty self explanitory
(defn match-card [instr]
  (when-let [[_ value' suit] (re-find #"^([2-9JQKA]|10)([hsdc])$" instr)]
    (let [value (keyword value')]
     (match [suit]
       ["h"] (list value :hearts)
       ["s"] (list value :spades)
       ["c"] (list value :clubs)
       ["d"] (list value :diamonds)
       :else nil))))

;; the parser which produces hands to be analyzed by assess-hand
;; verifies that match-card didn't return a nil for any entry, as that would invalidate the hand
(defn parse-hand [instr]
  (let [elems (split instr #"\s")]
    (cond
      (> 5 (count elems))       [:parse_error "please supply the line in the form #$ #$ #$ #$ #$"]
      (> 5 (count (set elems))) [:parse_error "multiples of same card not allowed"]
      :else (let [result (map match-card elems)]
              (if (some #(= nil %) result)
                [:parse_error "tokenization failed"]
                result)))))

;; we're checking for pairs, so single cards don't matter, so discard them then
;; break the list of card values and occurrences up into something that we can easily pattern match
(defn find-pairs [nums]
  (let [pairs (->> nums
               (filter #(not= 1 (val %)))
               (flatten)
               (vec))]
  (if (= 2 (count pairs)) ;; match for vectors is annoying in that it will only handle patterns of the same length
                          ;; so we break it up into two separate matches
    (match [pairs]
      [[a 2]]     {:result [:pair a] :text (format "pair of %s's" (name a))}
      [[a 3]]     {:result [:three a] :text (format "three %s's" (name a))}
      [[a 4]]     {:result [:four a] :text (format "four %s's" (name a))})
    (match [pairs]
      [[a 2 b 2]] {:result [:two-pair a b] :text (format "two pair : %s's and %s's" (name a) (name b))}
      [[a 2 b 3]] {:result [:full-house b a] :text (format "full house : %s's over %s's" (name b) (name a))}
      [[b 3 a 2]] {:result [:full-house b a] :text (format "full house : %s's over %s's" (name b) (name a))}))))

(defn keytonum [keyw]
    (match [keyw]
      [(:or :2 :3 :4 :5 :6 :7 :8 :9 :10)] (Integer. (name keyw))
      [:J] 11
      [:Q] 12
      [:K] 13
      [:A] 14))

(defn numtokey [number]
  (if (and (<= 2 number) (>= 10 number))
    (keyword number)
    (match [number]
      [11] :J
      [12] :Q
      [13] :K
      [(:or 14 1)] :A)))

; to handle the case where Ace is high or low in a straight, we
; first check the default case, which is 1, if we don't have a straight
; then check to see if we have a 1, change it to 14, then try again
(defn find-straight [hand]
  (loop [nums (apply sorted-set (map (comp keytonum first) hand))]
    (if (= 4 (- (last nums) (first nums)))
      {:result [:straight] :text "straight"}
      (if (contains? nums 14)
        (recur (conj (disj nums 14) 1))
        {:result [] :text ""}))))

(defn find-flush [hand result]
  (let [suits (set (map second hand))]
      (if (= 1 (count suits))
        (assoc result :result (conj (:result result) :flush) :text (str (:text result) " flush"))
        result)))

(defn default-high [hand result]
  (if (empty? (:result result))
    (let [high (last (apply sorted-set (map (comp keytonum first) hand)))]
      (assoc result :result [:high-card (numtokey high)] :text (str "high card: " (name (numtokey high)))))
    result))

(defn count-occurrences [hand]
  (reduce (partial merge-with +) {} (map #(assoc {} (first %) 1) hand)))

;;
;; the main poker logic function
;;
;; we make a quick check to see if we should be
;; looking for pairs or straight/flush by counting
;; the occurences of each card value, if any occurs
;; more than once, then a straight/flush is not possible
;; and we look only for multiples
;;
(defn assess-hand [hand]
  (let [nums (count-occurrences hand)]
    (if (> 5 (count nums))
      (find-pairs nums)
      (->> (find-straight hand)
           (find-flush hand)
           (default-high hand)))))

(defn get-input [prompt]
  (print prompt)
  (flush)
  (read-line))

(defn -main [& args]
  (println "Enter a hand of cards in the form of #$ #$ #$ #$ #$ where # is the card value and $ is one of [dhcs] representing the suit")
  (println "Empty line quits")
  (loop [line (get-input "|> ")]
    (when-not (empty? line)
      (let [hand (parse-hand line)]
        (if (= :parse_error (first hand))
          (println (pr-str hand))
          (println (str "Result : " (:text (assess-hand hand)))))
        (recur (get-input "|> "))))))


