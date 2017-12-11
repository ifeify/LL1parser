# LL1 grammar parser
LL1 grammar is a context free grammar that eliminates the need for backtracking
in a top down parser. This parser recognizes and evaluates simple boolean expressions.

The LL1 grammar is defined below:
```
S -> A
A -> BA'
A' -> |BA'
A' -> epsilon
B -> CB'
B' -> ^CB'
B' -> epsilon
C -> ~D
C -> D
D -> (A)
D -> 0
D -> 1
```

Examples of valid expressions are:

`~1` which gives 0

`(1)` which gives 1

`0 ^ 1 ^ 1` which gives 0

`1 | 0 ^ 1` evaluates to 1. Precedence is given to the ^ operator

The parse table is what dictates the whole parsing process. In order to build it,
we compute the first and follow sets of each non terminal.

The first sets are listed below:
```
First(S)= First(A) = First(B) = First(C) U First(D) = {~, (, 0, 1}
```