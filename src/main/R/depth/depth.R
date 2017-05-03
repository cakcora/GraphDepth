---
title: "depth"
output: html_document
---

```{r setup, include=FALSE}
knitr::opts_chunk$set(echo = TRUE)
require(ddalpha)

```


```{r}
data <- mvrnorm(1000, rep(0, 5),
matrix(c(
1, 0, 0, 0, 0,
0, 2, 0, 0, 0,
0, 0, 30, 0, 0,
0, 0, 0, 2, 0,
0, 0, 0, 0, 1),
nrow = 5))
x <- mvrnorm(10, rep(1, 5),
matrix(c(
1, 0, 0, 0, 0,
0, 1, 0, 0, 0,
0, 0, 1, 0, 0,
0, 0, 0, 1, 0,
0, 0, 0, 0, 1),
nrow = 5))
depths <- depth.(x, data, notion = "Mahalanobis")
cat("Depths: ", depths, "\n")
```
```{r}
par(mfrow = c(2,2))
data(hemophilia)
depth.contours(hemophilia[,1:2], depth = "none", main = "data")
for (depth in c("zonoid", "Mahalanobis", "projection", "spatial")){
depth.contours(hemophilia[,1:2], depth = depth, main = depth)
}
for (depth in c("halfspace", "simplicial", "simplicialVolume")){
depth.contours(hemophilia[,1:2], depth = depth, main = depth, exact = T)
}
```

  