/**    / \____  _    ______   _____ / \____   ____  _____
 *    /  \__  \/ \  / \__  \ /  __//  \__  \ /    \/ __  \   Javaslang
 *  _/  // _\  \  \/  / _\  \\_  \/  // _\  \  /\  \__/  /   Copyright 2014-2015 Daniel Dietrich
 * /___/ \_____/\____/\_____/____/\___\_____/_/  \_/____/    Licensed under the Apache License, Version 2.0
 */
package javaslang;

/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
   G E N E R A T O R   C R A F T E D
\*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/

import javaslang.algebra.HigherKinded13;
import javaslang.algebra.Monad13;

import java.util.Objects;

/**
 * Implementation of a pair, a tuple containing 13 elements.
 */
public class Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> implements Tuple, Monad13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, Tuple13<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>> {

    private static final long serialVersionUID = 1L;

    public final T1 _1;
    public final T2 _2;
    public final T3 _3;
    public final T4 _4;
    public final T5 _5;
    public final T6 _6;
    public final T7 _7;
    public final T8 _8;
    public final T9 _9;
    public final T10 _10;
    public final T11 _11;
    public final T12 _12;
    public final T13 _13;

    public Tuple13(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12, T13 t13) {
        this._1 = t1;
        this._2 = t2;
        this._3 = t3;
        this._4 = t4;
        this._5 = t5;
        this._6 = t6;
        this._7 = t7;
        this._8 = t8;
        this._9 = t9;
        this._10 = t10;
        this._11 = t11;
        this._12 = t12;
        this._13 = t13;
    }

    @Override
    public int arity() {
        return 13;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, MONAD extends HigherKinded13<U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13, Tuple13<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>>> Tuple13<U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13> flatMap(javaslang.function.Lambda13<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, ? super T11, ? super T12, ? super T13, MONAD> f) {
        return (Tuple13<U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13>) f.apply(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13> Tuple13<U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13> map(javaslang.function.Lambda13<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, ? super T11, ? super T12, ? super T13, Tuple13<? extends U1, ? extends U2, ? extends U3, ? extends U4, ? extends U5, ? extends U6, ? extends U7, ? extends U8, ? extends U9, ? extends U10, ? extends U11, ? extends U12, ? extends U13>> f) {
        // normally the result of f would be mapped to the result type of map, but Tuple.map is a special case
        return (Tuple13<U1, U2, U3, U4, U5, U6, U7, U8, U9, U10, U11, U12, U13>) f.apply(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13);
    }

    @Override
    public Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> unapply() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Tuple13)) {
            return false;
        } else {
            final Tuple13 that = (Tuple13) o;
            return Objects.equals(this._1, that._1)
                  && Objects.equals(this._2, that._2)
                  && Objects.equals(this._3, that._3)
                  && Objects.equals(this._4, that._4)
                  && Objects.equals(this._5, that._5)
                  && Objects.equals(this._6, that._6)
                  && Objects.equals(this._7, that._7)
                  && Objects.equals(this._8, that._8)
                  && Objects.equals(this._9, that._9)
                  && Objects.equals(this._10, that._10)
                  && Objects.equals(this._11, that._11)
                  && Objects.equals(this._12, that._12)
                  && Objects.equals(this._13, that._13);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)", _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13);
    }
}