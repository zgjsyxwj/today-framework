/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2021 All Rights Reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */

package cn.taketoday.web.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.taketoday.lang.Assert;

/**
 * list of {@link Validator}
 *
 * @author TODAY 2021/3/21 21:19
 * @since 3.0
 */
public class WebValidator {

  private boolean iterateAllValidators;

  private final List<Validator> validators;

  public WebValidator() {
    this(new ArrayList<>());
  }

  public WebValidator(List<Validator> validators) {
    this.validators = validators;
  }

  public void validate(final Object object, final Errors errors) {
    final boolean iterateAllValidators = isIterateAllValidators();
    for (final Validator validator : validators) {
      if (validator.supports(object)) {
        validator.validate(object, errors);
        if (!iterateAllValidators) {
          break;
        }
      }
    }
  }

  public void addValidator(Validator validator) {
    Assert.notNull(validator, "validator must not be null");
    validators.add(validator);
  }

  public void addValidators(Validator... validators) {
    Assert.notNull(validators, "validator must not be null");
    Collections.addAll(this.validators, validators);
  }

  public void addValidators(List<Validator> validators) {
    Assert.notNull(validators, "validator must not be null");
    this.validators.addAll(validators);
  }

  public List<Validator> getValidators() {
    return validators;
  }

  public void setIterateAllValidators(boolean iterateAllValidators) {
    this.iterateAllValidators = iterateAllValidators;
  }

  public boolean isIterateAllValidators() {
    return iterateAllValidators;
  }
}
