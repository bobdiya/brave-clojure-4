;; Excercises from Chapter 4 of book "Clojure for Brave & True"
;; https://www.braveclojure.com/core-functions-in-depth

;; Copying the code from the book, solutions to the excercise follows A Vampire
;; Data Analysis Program for the FWPD

;;; I'm keeping with our previous system of semicolons!
;;;
;;; Initial impressions: I think it's been a good idea that you've created a
;;; proper Clojure project for playing around with.

(ns fwpd.core)

(def filename "suspects.csv")
;;; Putting your file in `/resources` and loading it with `(slurp
;;; (clojure.java.io/resource "suspects.csv"))` would make it work after you
;;; build a jar. I see that the task asks you to do it directly on toplevel,
;;; though.
;;;
;;; Another note: I see that your lines end with a comma:
;;;
;;;     Anil,12,
;;;            ^ here
;;;
;;; I suspect that might give us some problems later on.

(def vamp-keys [:name :glitter-index])

(defn str->int
  [str]
  (Integer. str))

(comment
  ;; Another option: clojure.edn/read-string

  (require 'clojure.edn)

  (clojure.edn/read-string "123")
  ;; => 123

  (def some-str
    "[\"something\" else :hello 99 99.99]")
  (clojure.edn/read-string some-str)
  ;; => ["something" else :hello 99 99.99]

  (map type (clojure.edn/read-string some-str))
  ;; => (java.lang.String
  ;;     clojure.lang.Symbol
  ;;     clojure.lang.Keyword
  ;;     java.lang.Long
  ;;     java.lang.Double)

  ;; In your case, just using Ingeger. is probably a better idea -- you'll get
  ;; an error thrown if something unexpected is encountered.

  )

(def conversions {:name identity
                  :glitter-index str->int})

(defn convert
  [vamp-key value]
  ((get conversions vamp-key) value))

(convert :glitter-index "3")
;; => 3

(defn parse
  "Convert a CSV into rows of columns"
  [string]
  (map #(clojure.string/split % #",")
       (clojure.string/split string #"\n")))

(comment
;;; Nice! Notes""
;;;
;;; There's a function for splitting lines in the standard library:
  (clojure.string/split-lines "first line\nsecond line")
  ;; => ["first line" "second line"]

;;; On windows, you might get \r\n newlines instead of \n
  (clojure.string/split "win line 1\r\nwin line 2" #"\n")
  ;; => ["win line 1\r" "win line 2"]

;;; the split-lines function handles this gracefully. You might also choose to
;;; change your regex.
  (clojure.string/split-lines "win line 1\r\nwin line 2")
  ;; => ["win line 1" "win line 2"]
  (clojure.string/split "win line 1\r\nwin line 2" #"\r?\n")
  ;; => ["win line 1" "win line 2"]

;;; I just noticed that this was given as example code. Bummer!
  )

(parse (slurp filename))
;; => (["Anil" "12" "\r"] ["Nethra" "0" "\r"] ["Kullu" "1" "\r"] ["Mullu" "7" "\r"])

;;; Note: when I'm running this on my linux system, I'm not seeing \r output:
(comment
  (parse (slurp filename))
  ;; => (["Anil" "12"] ["Nethra" "0"] ["Kullu" "1"] ["Mullu" "7"])
  )

(comment
  (clojure.string/split-lines (slurp filename))
  ;; => ["Anil,12," "Nethra,0," "Kullu,1," "Mullu,7,"]
  )


(defn mapify
  "Return a seq of maps like {:name \"Edward Cullen\" :glitter-index 10}"
  [rows]
  (map (fn [unmapped-row]
         (reduce (fn [row-map [vamp-key value]]
                   (assoc row-map vamp-key (convert vamp-key value)))
                 {}
                 (map vector vamp-keys unmapped-row)))
       rows))

(first (mapify (parse (slurp filename))))
;; => {:name "Anil", :glitter-index 12}

(comment
;;; Nice! I dont' think we should need an inner reduce _and_ a map, though. Have
;;; you seen that you can iterate over multiple seqences at the same time with map?

  (map (fn [num numtype] {:num num
                          :numtype numtype})
       (range 6)              ; first arg into fn -> num
       (cycle [:even :odd])   ; second arg into fn -> numtype
       )
;; => ({:num 0, :numtype :even}
;;     {:num 1, :numtype :odd}
;;     {:num 2, :numtype :even}
;;     {:num 3, :numtype :odd}
;;     {:num 4, :numtype :even}
;;     {:num 5, :numtype :odd})

;;; map then stops when one sequence has no more values
  (map vector [1 2 3 4 5 6 7 8 9 10 11] ["One" "Two"])
  ;; => ([1 "One"] [2 "Two"])

;;; if we want to return a map (datatype), we can use (into {} ,,,)
  (into {}
        (map vector [1 2 3 4 5 6 7 8 9 10 11] ["One" "Two"]))
  ;; => {1 "One", 2 "Two"}

;;; Which should let us do something like this:
  (map (fn [row]
         (into {}
               (map (fn [vamp-key value]
                      [vamp-key (convert vamp-key value)])
                    vamp-keys
                    row)))
       (parse (slurp filename)))
  ;; => ({:name "Anil", :glitter-index 12}
  ;;     {:name "Nethra", :glitter-index 0}
  ;;     {:name "Kullu", :glitter-index 1}
  ;;     {:name "Mullu", :glitter-index 7})

;;; Though that didn't make too much of a difference. You be the judge!
;;;
;;; ... and again, I see that I've wanted to rewrite code from the book ...
  )


(defn glitter-filter
  [minimum-glitter records]
  (filter #(>= (:glitter-index %) minimum-glitter) records))

(glitter-filter 3 (mapify (parse (slurp filename))))
;; => ({:name "Anil", :glitter-index 12} {:name "Mullu", :glitter-index 7})


;; Excercises
;; 1. Turn the result of your glitter filter into a list of names.

(defn glitter-filter-names
  [min-glitter records]
  (map :name (glitter-filter min-glitter records)))

(glitter-filter-names 3 (mapify (parse (slurp filename))))
;; => ("Anil" "Mullu")

;;; Nice!

;; 2. Write a function, append, which will append a new suspect to your list of
;; suspects.
(def records [{:name "Anil" :glitter-index 2}
              {:name "Pradeep" :glitter-index 12}])

(defn append [suspects suspect-to-add]
  (conj suspects suspect-to-add))

(append records {:name "Anurag" :glitter-index 21})
;; => [{:name "Anil", :glitter-index 2}
;;     {:name "Pradeep", :glitter-index 12}
;;     {:name "Anurag", :glitter-index 21}]

;;; Nice!

;; 3. Write a function, validate, which will check that :name and :glitter-index
;; are present when you append. The validate function should accept two
;; arguments: a map of keywords to validating functions, similar to conversions,
;; and the record to be validated.

(defn presence [param]
  (if (nil? param)
    false
    true))

(def suspect-validator {:name presence :glitter-index presence})

(defn validate [key-func-map record]
  (reduce
   (fn [result [key val]]
     (and result ((get key-func-map key) (key record))))
   true
   key-func-map))

(validate suspect-validator  {:name "Anil"})
;; => false

(validate suspect-validator  {:name "Anil" :glitter-index 10})
;; => true

(comment
;;; Nice!
;;;
;;; If you want to make validate more compact, you migth be able to make use of
;;; `every?`.

(doc every?)
"-------------------------
clojure.core/every?
([pred coll])
  Returns true if (pred x) is logical true for every x in coll, else
  false."
  )

;; 4. Write a function that will take your list of maps and convert it back to a
;; CSV string. You’ll need to use the clojure.string/join function.

(defn csv-convert [records]
  (reduce (fn [arg1 arg2] (str arg1 "\n" arg2))
          (map (fn [record]
                 (clojure.string/join "," [(:name record)
                                           (:glitter-index record)]))
          records)))

(csv-convert records);; => "Anil,2\nPradeep,12"

(comment
;;; Nice!
;;;
;;; Could you have used clojure.string/join in the outer loop too?
  )
