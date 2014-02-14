require(ggplot2)

points = c(1,2,4,8)
types = c("MPI", "omp")
omp_points = c(1,4,8)

df = NULL

errorUpper <- function(x){ 
        return(mean(x) + sd(x))
} 

errorLower <- function(x){ 
        return(mean(x) - sd(x))
} 

prepare_scale_y <- function(vec, step, breakOnAll) {
    a <- round(min(vec)/step)*step
    b <- round(max(vec)/step)*step
    brks <- seq(a, b, by=step)
    if(breakOnAll)
        return(scale_y_continuous(breaks = brks, minor_breaks=a:b))
    else
        return(scale_y_continuous(breaks = brks))
}

for(type in types) {
    for(f in points) {
        read <- read.table(file=paste(c('runs_',type, "_", f, "thread.txt.parsed"),collapse=""))
        rbind(df, data.frame(x=f, y=read$V1/100, t=type)) -> df
    }
}

p <- ggplot(data=df, aes(x=factor(x),y=y,color=t)) + stat_summary(fun.y="mean", geom="point", size=5)
p = p + stat_summary(fun.ymax = errorUpper, fun.ymin = errorLower, geom = "errorbar", size=0.5, width=.15)
p = p + xlab("Number of threads (OpenMP threads or MPI processes)") + ylab("Execution time [s]")
p = p + scale_color_brewer(palette="Set1")
p = p + scale_fill_brewer(palette="Set1")
p = p + prepare_scale_y(df$y, 0.1, FALSE)
p = p + theme(axis.text=element_text(colour="black"))
p = p + theme(legend.title=element_blank())
p = p + theme(axis.title.x=element_text(size=16)) + theme(axis.title.y=element_text(size=16))
p = p + theme(axis.text=element_text(size=14))

ggsave("extime.pdf", p)
