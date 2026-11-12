import { lazy, Suspense, useMemo } from 'react'
import { LoaderCircle } from 'lucide-react'
import { makeLanyardFace } from '../lib/lanyardFace'

const Lanyard = lazy(() => import('./Lanyard'))

export default function IdentityPass({ user, photo, className = '' }) {
  const frontImage = useMemo(() => makeLanyardFace({
    name: user?.fullName, email: user?.email,
    role: user?.role?.replaceAll('_', ' ') || 'MEMBER', photo, verified: true,
  }), [user?.fullName, user?.email, user?.role, photo])

  return <div className={`identity-pass ${className}`}>
    <Suspense fallback={<div className="lanyard-loading"><LoaderCircle className="spinner" size={18}/> Preparing your pass…</div>}>
      <Lanyard frontImage={frontImage} orientation="portrait" finish="glossy" cornerRadius={0.34} size={0.68} strapColor="#5558d6" strapWidth={0.72} metal="silver" gravity={0.8} elasticity={0.48} damping={0.56} breeze={0.12} interactive intro className="settings-lanyard" />
    </Suspense>
  </div>
}
