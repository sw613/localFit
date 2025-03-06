import { styled } from '@mui/material/styles';
import { Stack, Card } from '@mui/material';

export const LoginContainer = styled(Stack)(({ theme }) => ({
  minHeight: '100vh',
  padding: theme.spacing(2),
  backgroundColor: theme.palette.background.default
}));

export const LoginCard = styled(Card)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  width: '100%',
  maxWidth: '400px',
  margin: '0 auto',
  padding: theme.spacing(3),
  boxShadow: theme.shadows[2]
}));