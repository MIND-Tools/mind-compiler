/**
 * Fractal Runtime
 *
 * Copyright (C) 2009 STMicroelectronics
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 */

#include "LCCdelegate.h"

int __component_getFcState_delegate(struct __component_LifeCycleState *state)
{
  if (state->state == 0)
    return FRACTAL_API_STOPPED;
  else
    return FRACTAL_API_STARTED;
}

int __component_startFc_delegate(struct __component_LifeCycleState *state)
{
  state->state = 1;
  return FRACTAL_API_OK;

}

int __component_stopFc_delegate(struct __component_LifeCycleState *state)
{
  state->state = 0;
  return FRACTAL_API_OK;
}
