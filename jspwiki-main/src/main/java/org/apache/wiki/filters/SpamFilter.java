package org.apache.wiki.filters;

import javax.annotation.Nullable;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.auth.user.UserProfile;

public interface SpamFilter
{
    public boolean isValidUserProfile( final Context context, final UserProfile profile );
}
