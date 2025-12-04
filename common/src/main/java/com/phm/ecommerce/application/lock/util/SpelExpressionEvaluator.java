package com.phm.ecommerce.application.lock.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpelExpressionEvaluator {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public static String evaluate(ProceedingJoinPoint joinPoint, String expression) {
        EvaluationContext context = createEvaluationContext(joinPoint);
        return PARSER.parseExpression(expression).getValue(context, String.class);
    }

    public static EvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return context;
    }
}
